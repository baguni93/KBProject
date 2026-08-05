package org.scoula.event.mapper;

import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.scoula.config.RootConfig;
import org.scoula.event.dto.EventResponseDTO;
import org.scoula.event.dto.UserChallengeDTO;
import org.scoula.security.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Log4j2
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class, SecurityConfig.class})
public class EventMapperTest {
    // test 코드생성 구글검색 참조함
    @Autowired
    private EventMapper eventMapper;

    @Test
    @DisplayName("1. 사용자 보유 포인트 조회 테스트")
    public void getUserPointTest() {
        Integer userId = 1;

        Integer point = eventMapper.getUserPoint(userId);

        log.info("====================================");
        log.info("유저 ID: " + userId + "의 보유 포인트: " + point);
        log.info("====================================");

        // 포인트 데이터가 null이 아니거나 0 이상인지 검증
        assertNotNull(point);
    }

    @Test
    @DisplayName("2. 사용자 최신 챌린지 정보 통합 조회 테스트")
    public void getUserChallengeStatusTest() {
        Integer noDataUserId = 1;
        UserChallengeDTO challengeNoData = eventMapper.getUserChallengeStatus(noDataUserId);

        assertNotNull(challengeNoData);
        assertEquals(1, challengeNoData.getUserChallengeLevel());
        assertEquals(0, challengeNoData.getUserChallengeExe());
        assertEquals(1000, challengeNoData.getUserChallengeMaxExe()); // 유저가 없으므로 1000 기대

        // [케이스 2] DB에 참여 데이터가 존재하는 유저 (기존 1번 유저)
        Integer hasDataUserId = 1;
        UserChallengeDTO challengeHasData = eventMapper.getUserChallengeStatus(hasDataUserId);

        assertNotNull(challengeHasData);
        // 1번 유저의 실제 DB 값에 맞게 검증 (예: DB 값이 10이면 10으로 수정)
        assertEquals(10, challengeHasData.getUserChallengeMaxExe());
    }

    @Test
    @DisplayName("3. 참여 가능한 이벤트 전체 리스트 조회 테스트")
    public void getActiveEventProgressListTest() {
        Integer userId = 1;

        List<EventResponseDTO> activeEvents = eventMapper.getActiveEventProgressList(userId);

        log.info("====================================");
        log.info("참여 가능한 이벤트 총 개수: " + (activeEvents != null ? activeEvents.size() : 0));
        if (activeEvents != null) {
            for (EventResponseDTO event : activeEvents) {
                log.info("이벤트 ID: " + event.getEventId() + " | 이름: " + event.getEventName() + " | 타입: " + event.getEventType());
            }
        }
        log.info("====================================");
    }

    @Test
    @DisplayName("4. 참여 완료한 이벤트 리스트 조회 테스트")
    public void getJoinedEventProgressListTest() {
        Integer userId = 1;
        String yearMonth = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));

        List<EventResponseDTO> joinedEvents = eventMapper.getJoinedEventProgressList(userId, yearMonth);

        log.info("====================================");
        log.info("참여 완료한 이벤트 총 개수: " + (joinedEvents != null ? joinedEvents.size() : 0));
        if (joinedEvents != null) {
            for (EventResponseDTO event : joinedEvents) {
                log.info("완료된 이벤트 ID: " + event.getEventId() + " | 이름: " + event.getEventName());
            }
        }
        log.info("====================================");
    }
}
