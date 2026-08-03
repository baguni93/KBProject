// 생년월일을 YYMMDD 형식으로 변환
const getBirthDatePin = (birthDate) => {
    if (!birthDate) return '';

    const digits = birthDate.replace(/[^0-9]/g, '');

    if (digits.length !== 8) return '';

    return digits.slice(2);
};

// 동일 숫자 4자리 이상 연속 확인
const hasRepeatedNumbers = (pin) => /(\d)\1{3,}/.test(pin);

// 오름차순 또는 내림차순 숫자 4자리 이상 확인
const hasSequentialNumbers = (pin) => {
    for (let start = 0; start <= pin.length - 4; start += 1) {
        let ascending = true;
        let descending = true;

        for (let index = start + 1; index < pin.length; index += 1) {
            const previous = Number(pin[index - 1]);
            const current = Number(pin[index]);

            if (current - previous !== 1) ascending = false;
            if (current - previous !== -1) descending = false;

            if (!ascending && !descending) break;
            if (index - start >= 3) return true;
        }
    }

    return false;
};

// PIN 유효성 검사
export const validatePin = (pin, birthDate) => {
    if (!/^\d{6}$/.test(pin)) {
        return {
            valid: false,
            message: '간편비밀번호는 숫자 6자리로 입력해 주세요.',
        };
    }

    const birthDatePin = getBirthDatePin(birthDate);

    if (birthDatePin && pin === birthDatePin) {
        return {
            valid: false,
            message: '생년월일은 간편비밀번호로 사용할 수 없습니다.',
        };
    }

    if (hasRepeatedNumbers(pin)) {
        return {
            valid: false,
            message: '동일한 숫자를 4자리 이상 연속으로 사용할 수 없습니다.',
        };
    }

    if (hasSequentialNumbers(pin)) {
        return {
            valid: false,
            message: '연속된 숫자를 4자리 이상 사용할 수 없습니다.',
        };
    }

    return {
        valid: true,
        message: '',
    };
};