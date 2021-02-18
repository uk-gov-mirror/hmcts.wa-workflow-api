package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKeys;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IdempotencyTaskServiceTest {

//    @Captor
//    ArgumentCaptor<IdempotentKeys> captor;
//
//
//    @BeforeEach
//    void setUp() {
//        String idempotentKey = "some idempotent key";
//        when(externalTask.getVariable("idempotentKey")).thenReturn(idempotentKey);
//
//        String jurisdiction = "some jurisdiction";
//        when(externalTask.getVariable("jurisdiction")).thenReturn(jurisdiction);
//
//        idempotentId = new IdempotentId(idempotentKey, jurisdiction);
//    }
//
//    @ParameterizedTest
//    @CsvSource({
//        "some process id,some process id,false",
//        "some process id,some other process id,true",
//    })
//    void handleIdempotentIdIsPresentInDbTest(String processIdRow, String processIdTask, boolean isDuplicate) {
//
//        when(idempotentKeysRepository.findById(idempotentId))
//            .thenReturn(Optional.of(new IdempotentKeys(
//                idempotentId,
//                processIdRow,
//                LocalDateTime.now(),
//                LocalDateTime.now()
//            )));
//
//        when(externalTask.getProcessInstanceId()).thenReturn(processIdTask);
//
//        idempotencyTaskWorker.checkIdempotency(externalTask, externalTaskService);
//
//        Map<String, Object> processVariables = singletonMap("isDuplicate", isDuplicate);
//        verify(externalTaskService).complete(externalTask, processVariables);
//    }
//
//    @Test
//    void handleIdempotentIdIsNotPresentInDbTest() {
//        when(idempotentKeysRepository.findById(idempotentId))
//            .thenReturn(Optional.empty());
//
//        when(externalTask.getProcessInstanceId()).thenReturn("some process id");
//
//        idempotencyTaskWorker.checkIdempotency(externalTask, externalTaskService);
//
//        verify(idempotentKeysRepository).save(captor.capture());
//
//        IdempotentKeys actualIdempotentKeys = captor.getValue();
//        assertThat(actualIdempotentKeys).isEqualToComparingOnlyGivenFields(
//            new IdempotentKeys(idempotentId, "some process id", null, null),
//            "idempotentId", "processId"
//        );
//
//        Map<String, Object> expectedProcessVariables = singletonMap("isDuplicate", false);
//        verify(externalTaskService).complete(externalTask, expectedProcessVariables);
//    }

}
