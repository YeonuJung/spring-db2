package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;


@Slf4j
@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LogRepository logRepository;

    /**
     * memberService    @Transactional: OFF
     * memberRepository @Transactional: ON (커밋)
     * logRepository    @Transactional: ON (커밋)
     */
    @Test
    void outerTxOff_success(){
        //given
        String username = "outerTxOff_success";
        //when
        memberService.joinV1(username);
        //then
        assertThat(memberRepository.find(username)).isNotEmpty(); // 커밋
        assertThat(logRepository.find(username)).isNotEmpty(); // 커밋

    }

    /**
     * memberService    @Transactional: OFF
     * memberRepository @Transactional: ON (커밋)
     * logRepository    @Transactional: ON (Exception 터짐 -> 롤백)
     */
    @Test
    void outerTxOff_fail(){
        //given
        String username = "로그예외_outerTxOff_fail";
        //when
        assertThatThrownBy(() -> memberService.joinV1(username)).isInstanceOf(RuntimeException.class);
        //then
        assertThat(memberRepository.find(username)).isNotEmpty(); // 커밋
        assertThat(logRepository.find(username)).isEmpty(); // 롤백

    }

    /**
     * memberService    @Transactional: ON
     * memberRepository @Transactional: OFF
     * logRepository    @Transactional: OFF
     */
    @Test
    void singleTx(){
        //given
        String username = "singleTx_success";
        //when
        memberService.joinV1(username);
        //then
        assertThat(memberRepository.find(username)).isNotEmpty(); // 커밋
        assertThat(logRepository.find(username)).isNotEmpty(); // 커밋

    }

    /**
     * memberService    @Transactional: ON
     * memberRepository @Transactional: ON
     * logRepository    @Transactional: ON
     */
    @Test
    void outer_TxOn_success(){
        //given
        String username = "outerTxOn_success";
        //when
        memberService.joinV1(username);
        //then
        assertThat(memberRepository.find(username)).isNotEmpty(); // 커밋
        assertThat(logRepository.find(username)).isNotEmpty(); // 커밋

    }

    /**
     * memberService    @Transactional: ON
     * memberRepository @Transactional: ON
     * logRepository    @Transactional: ON (Exception 터짐)
     */
    @Test
    void outer_TxOn_fail(){
        //given
        String username = "로그예외_outerTxOn_fail";
        //when
        assertThatThrownBy(() -> memberService.joinV1(username)).isInstanceOf(RuntimeException.class);
        //then
        assertThat(memberRepository.find(username)).isEmpty(); // 롤백됨(로그 레파지토리 때문에)
        assertThat(logRepository.find(username)).isEmpty(); // rollback-Only 마킹

    }


    /**
     * memberService    @Transactional: ON
     * memberRepository @Transactional: ON
     * logRepository    @Transactional: ON (Exception 터짐 but 예외 복구)
     */
    @Test
    void recoverException_fail(){
        //given
        String username = "로그예외_recoverException_fail";
        //when
        assertThatThrownBy(() -> memberService.joinV2(username)).isInstanceOf(UnexpectedRollbackException.class);
        //then
        assertThat(memberRepository.find(username)).isEmpty(); // 롤백됨
        assertThat(logRepository.find(username)).isEmpty(); // 예외 복구했으나 rollback-Only때문에 롤백됨

    }

    /**
     * memberService    @Transactional: ON
     * memberRepository @Transactional: ON
     * logRepository    @Transactional: ON (전파 수준 -> REQUIRES_NEW, Exception 터짐)
     */
    @Test
    void recoverException_success(){
        //given
        String username = "로그예외_recoverException_success";
        //when
        memberService.joinV2(username);
        //then
        assertThat(memberRepository.find(username)).isNotEmpty(); // 커밋됨
        assertThat(logRepository.find(username)).isEmpty(); // 별도의 트랜잭션 사용(롤백)

    }
}