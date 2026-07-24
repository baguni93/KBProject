package org.scoula.pointwallet.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.pointwallet.dto.PointTransactionDTO;
import org.scoula.pointwallet.dto.PointWalletDTO;

import java.util.List;


public interface PointWalletMapper {

    PointWalletDTO selectWalletByUserId(
            @Param("userId") Integer userId
    );

    List<PointTransactionDTO> selectTransactionsByUserId(
            @Param("userId") Integer userId
    );
}