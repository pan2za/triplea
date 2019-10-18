package org.triplea.lobby.server.db.dao;

import java.util.Optional;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.triplea.lobby.server.db.data.UserRoleLookup;

/** Data access object for the users table. */
public interface UserJdbiDao {
  @SqlQuery("select id from lobby_user where username = :username")
  Optional<Integer> lookupUserIdByName(@Bind("username") String username);

  @SqlQuery("select bcrypt_password from lobby_user where username = :username")
  Optional<String> getPassword(@Bind("username") String username);

  @SqlUpdate("update lobby_user set last_login = now() where username = :username")
  int updateLastLoginTime(@Bind("username") String username);

  @SqlUpdate("update lobby_user set bcrypt_password = :newPassword where id = :userId")
  int updatePassword(@Bind("userId") int userId, @Bind("newPassword") String newPassword);

  @SqlQuery("select email from lobby_user where id = :userId")
  String fetchEmail(@Bind("userId") int userId);

  @SqlUpdate("update lobby_user set email = :newEmail where id = :userId")
  int updateEmail(@Bind("userId") int userId, @Bind("newEmail") String newEmail);

  @SqlQuery(
      "select "
          + "   id as "
          + UserRoleLookup.USER_ID_COLUMN
          + ",   user_role_id as "
          + UserRoleLookup.USER_ROLE_ID_COLUMN
          + " from lobby_user "
          + " where username = :username")
  Optional<UserRoleLookup> lookupUserRoleByName(@Bind("username") String username);
}