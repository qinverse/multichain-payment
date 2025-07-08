import React, { useState } from 'react';
// v6 新用法
import { BrowserProvider, parseEther } from 'ethers';


function App() {
  const [account, setAccount] = useState('');
  const [amount, setAmount] = useState('0.001'); // 默认0.001 ETH
  const [orderNo, setOrderNo] = useState('');
  const [merchantAddress, setMerchantAddress] = useState('');

  // 连接钱包
  async function connectWallet() {
    if (!window.ethereum) {
      alert('请先安装 MetaMask 钱包插件');
      return;
    }
    try {
      const accounts = await window.ethereum.request({ method: 'eth_requestAccounts' });
      setAccount(accounts[0]);
    } catch (err) {
      alert('连接钱包失败');
    }
  }

  // 创建订单并发起支付
  async function createOrderAndPay() {
    if (!account) {
      alert('请先连接钱包');
      return;
    }

    // 1. 调用后端接口创建订单
    const createRes = await fetch('/api/order/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        channelCode: 'eth',
        from: account,
        amount: parseFloat(amount),
      }),
    });
    const order = await createRes.json();
    setOrderNo(order.paySeq);
    setMerchantAddress(order.toAddress);

    // 2. 使用 ethers.js 发起转账
    const provider = new BrowserProvider(window.ethereum);
    const signer = await provider.getSigner();

    try {
      const tx = await signer.sendTransaction({
        to: merchantAddress,
        value: parseEther(amount),
      });
      alert('交易已发出，交易哈希: ' + tx.hash);

      // 3. 把 txHash 回传给后端
      await fetch('/api/order/updateTxHash', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ paySeq: order.paySeq, thirdIdentify: tx.hash }),
      });
    } catch (err) {
      alert('交易发送失败：' + err.message);
    }
  }

  return (
      <div style={{ padding: 20 }}>
        <button onClick={connectWallet}>连接钱包</button>
        <div>当前钱包地址：{account}</div>
        <input
            type="number"
            step="0.001"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            placeholder="支付金额(ETH)"
        />
        <button onClick={createOrderAndPay}>创建订单并支付</button>
        <div>订单号：{orderNo}</div>
      </div>
  );
}

export default App;
