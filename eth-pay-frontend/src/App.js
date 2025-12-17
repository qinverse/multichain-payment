import React, { useState, useEffect } from 'react';
import { BrowserProvider, parseEther, formatEther } from 'ethers';
import './App.css';

const SEPOLIA_CHAIN_ID = 11155111; // Sepolia 测试网

function App() {
  const [account, setAccount] = useState('');
  const [amount, setAmount] = useState('0.0001');
  const [paySeq, setPaySeq] = useState('');
  const [nonce, setNonce] = useState('');
  const [merchantAddress, setMerchantAddress] = useState('');
  const [loading, setLoading] = useState(false);
  const [network, setNetwork] = useState({ name: '未知', chainId: null });

  /** 页面加载：同步钱包状态（✅ 修复点） */
  useEffect(() => {
    if (!window.ethereum) return;

    const syncWalletState = async () => {
      try {
        // 直接问 MetaMask
        const chainIdHex = await window.ethereum.request({ method: 'eth_chainId' });
        const chainId = parseInt(chainIdHex, 16);

        setNetwork({
          name: chainId === SEPOLIA_CHAIN_ID ? 'Sepolia' : '未知',
          chainId
        });

        const accounts = await window.ethereum.request({ method: 'eth_accounts' });
        if (accounts.length > 0) {
          setAccount(accounts[0]);
        }
      } catch (e) {
        console.error('同步钱包状态失败:', e);
      }
    };

    syncWalletState();

    const handleChainChanged = () => {
      syncWalletState();
    };

    const handleAccountsChanged = () => {
      syncWalletState();
    };

    window.ethereum.on('chainChanged', handleChainChanged);
    window.ethereum.on('accountsChanged', handleAccountsChanged);

    return () => {
      window.ethereum.removeListener('chainChanged', handleChainChanged);
      window.ethereum.removeListener('accountsChanged', handleAccountsChanged);
    };
  }, []);

  /** 页面加载：补偿 txHash */
  useEffect(() => {
    const savedPaySeq = localStorage.getItem('current_pay_seq');
    if (!savedPaySeq) return;

    const txHash = localStorage.getItem(`pay_tx_${savedPaySeq}`);
    if (!txHash) return;

    fetch('/api/order/updateTxHash', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        paySeq: savedPaySeq,
        thirdIdentify: txHash,
        nonce: nonce,
      }),
    })
        .then(() => {
          console.log('txHash 补偿回传成功:', txHash);
        })
        .catch(() => {});
  }, [nonce]);

  /** 连接钱包 */
  async function connectWallet() {
    if (!window.ethereum) {
      alert('请先安装 MetaMask');
      return;
    }

    try {
      const accounts = await window.ethereum.request({ method: 'eth_requestAccounts' });
      setAccount(accounts[0]);

      const chainIdHex = await window.ethereum.request({ method: 'eth_chainId' });
      const chainId = parseInt(chainIdHex, 16);

      setNetwork({
        name: chainId === SEPOLIA_CHAIN_ID ? 'Sepolia' : '未知',
        chainId
      });
    } catch (e) {
      console.error('连接钱包失败:', e);
      alert('连接钱包失败: ' + e.message);
    }
  }

  /** 创建订单并支付 */
  async function createOrderAndPay() {
    if (!account) return alert('请先连接钱包');
    if (loading) return;

    try {
      setLoading(true);

      // 1️⃣ 创建订单
      const res = await fetch('/api/order/create', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          channelCode: 'eth',
          from: account,
          amount: parseFloat(amount),
        }),
      });

      const result = await res.json();
      if (!result.success) {
        throw new Error(result.msg || '创建订单失败');
      }

      const order = result.data;
      setPaySeq(order.paySeq);
      setMerchantAddress(order.toAddress);
      setNonce(order.nonce);
      localStorage.setItem('current_pay_seq', order.paySeq);

      // 2️⃣ 链上交易前校验
      const chainIdHex = await window.ethereum.request({ method: 'eth_chainId' });
      const chainId = parseInt(chainIdHex, 16);
      if (chainId !== SEPOLIA_CHAIN_ID) {
        throw new Error('请切换到 Sepolia 测试网');
      }

      const provider = new BrowserProvider(window.ethereum);
      const signer = await provider.getSigner();

      const value = parseEther(amount);
      const balance = await provider.getBalance(account);
      if (balance < value) {
        throw new Error(`余额不足，当前余额 ${formatEther(balance)} ETH`);
      }

      // 3️⃣ 发交易
      const tx = await signer.sendTransaction({
        to: order.toAddress,
        value,
      });

      localStorage.setItem(`pay_tx_${order.paySeq}`, tx.hash);

      // 4️⃣ 回传 txHash
      fetch('/api/order/updateTxHash', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          paySeq: order.paySeq,
          thirdIdentify: tx.hash,
          nonce: tx.nonce,
        }),
      }).catch(() => {
        console.warn('txHash 回传失败，等待页面补偿');
      });

      alert('交易已提交，等待区块确认');
    } catch (e) {
      console.error(e);
      alert(e.message || '支付失败');
    } finally {
      setLoading(false);
    }
  }

  return (
      <div className="page">
        <div className="card">
          <h2 className="title">ETH 支付（Sepolia）</h2>

          <div className="section">
            {account ? (
                <div className="wallet-box">
                  <div className="label">当前钱包</div>
                  <div className="address">{account}</div>
                  <div>网络: {network?.name}</div>
                  <div>chainId: {network?.chainId}</div>
                </div>
            ) : (
                <button className="btn primary" onClick={connectWallet}>
                  连接钱包
                </button>
            )}
          </div>

          <div className="section">
            <label className="label">支付金额 (ETH)</label>
            <input
                className="input"
                type="number"
                step="0.001"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
            />
          </div>

          <button
              className="btn primary"
              disabled={!account || loading}
              onClick={createOrderAndPay}
          >
            {loading ? '处理中...' : '创建订单并支付'}
          </button>

          {paySeq && (
              <div className="order-box">
                <div>
                  <span>订单号：</span>
                  <strong>{paySeq}</strong>
                </div>
                <div>
                  <span>收款地址：</span>
                  <div className="address">{merchantAddress}</div>
                </div>
              </div>
          )}
        </div>
      </div>
  );
}

export default App;
