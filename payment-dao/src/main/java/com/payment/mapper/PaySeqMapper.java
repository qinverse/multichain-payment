package com.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.payment.common.base.PayOrderStatusEnum;
import com.payment.model.entity.PaySeqEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PaySeqMapper extends BaseMapper<PaySeqEntity> {
    default List<PaySeqEntity> selectNotFinalConfirmed() {
        return selectList(
                Wrappers.lambdaQuery(PaySeqEntity.class)
                        .and(w -> w
                                .notIn(
                                        PaySeqEntity::getStatus,
                                        PayOrderStatusEnum.PAY_SUCCESS.getValue(),
                                        PayOrderStatusEnum.PAY_FAIL.getValue(),
                                        PayOrderStatusEnum.PAY_TIMEOUT.getValue(),
                                        PayOrderStatusEnum.PAY_FINISHED.getValue()
                                )
                                .or()
                                .eq(PaySeqEntity::getStatus, PayOrderStatusEnum.PAY_SUCCESS.getValue())
                                .isNull(PaySeqEntity::getConfirmedAt)
                        )
        );
    }

    default PaySeqEntity selectById(String seq) {
        return selectOne(
                Wrappers.lambdaQuery(PaySeqEntity.class)
                        .and(w -> w.eq(PaySeqEntity::getPaySeq, seq)));
    }

    default int updateById(PaySeqEntity entity) {
        return update(entity, Wrappers.lambdaQuery(PaySeqEntity.class)
                .and(w -> w.eq(PaySeqEntity::getPaySeq, entity.getPaySeq())));
    }
}
