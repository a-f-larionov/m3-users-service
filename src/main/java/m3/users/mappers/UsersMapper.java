package m3.users.mappers;

import org.mapstruct.Mapper;

import m3.users.dto.rs.UpdateUserInfoRsDto;
import m3.users.entities.UserEntity;

@Mapper(componentModel = "spring")
public interface UsersMapper {

        public UpdateUserInfoRsDto toDto(UserEntity user);
}