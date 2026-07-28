package org.scoula.remittance.service;

import org.scoula.remittance.dto.RemittanceDTO;

public interface RemittanceService {

    boolean sendMoney(RemittanceDTO remittanceDTO);

}
