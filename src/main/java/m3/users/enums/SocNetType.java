package m3.users.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum SocNetType {

    VK(1L),
    Standalone(2L);

    @JsonValue
    private final Long id;

    public static SocNetType of(Long socNetTypeId) {
        return Stream.of(SocNetType.values())
                .filter(socNetType -> socNetType.getId().equals(socNetTypeId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
