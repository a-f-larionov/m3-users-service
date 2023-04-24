package m3.users.mappers;

import m3.users.dto.rq.AuthRqDto;
import m3.users.dto.rs.AuthSuccessRsDto;
import m3.users.dto.rs.UpdateUserInfoRsDto;
import m3.users.entities.UserEntity;
import m3.users.enums.SocNetType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsersMapper {

    UpdateUserInfoRsDto entityToDto(UserEntity user);

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "socNetType", source = "socNetTypeId")
    AuthSuccessRsDto entityToAuthSuccessRsDto(UserEntity outUser);

    default SocNetType map(Long value) {
        return SocNetType.of(value);
    }

    @Mapping(target = "createTm", source = "currentTm")
    @Mapping(target = "loginTm", source = "currentTm")
    @Mapping(target = "fullRecoveryTime", source = "currentTm")
    @Mapping(target = "socNetTypeId", source = "authRqDto.socNetType")
    UserEntity forAuthNewUser(AuthRqDto authRqDto, Long currentTm, Long nextPointId);

    default Long map(SocNetType socNetType) {
        return socNetType.getId();
    }
}