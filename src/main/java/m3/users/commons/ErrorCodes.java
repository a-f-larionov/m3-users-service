package m3.users.commons;

import lombok.Getter;

@Getter
public enum ErrorCodes {


    AUTH_FAILED(1, "Авторизация не удалась.");

    private Integer code;
    private String message;

    ErrorCodes(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
