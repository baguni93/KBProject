package org.scoula.settlement.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.settlement.domain.SettlementMemberVO;
import org.scoula.settlement.domain.SettlementVO;

import java.util.List;

public interface SettlementMapper {

    void create(SettlementVO settlementVO);

    SettlementVO get(int settlementId);

    void createMember(SettlementMemberVO member);

    List<SettlementVO> getMyList(int settlementId);

    void completeMemberSettlement(@Param("settlementId") int settlementId,
                                  @Param("userId") int userId);

    boolean isAllMemberCompleted(int settlementId);

    void completeSettlement(int settlementId);
    void cancelSettlement(int settlementId);
    void cancelMemberSettlement(int settlementId);

    boolean canCancelSettlement(@Param("settlementId") int settlementId,
                                @Param("userId") int userId);


    void updateLastReminderDate(int settlementId);
}
