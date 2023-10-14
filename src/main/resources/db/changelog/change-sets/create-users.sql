CREATE TABLE IF NOT EXISTS `users`
(
    `id`               int unsigned NOT NULL AUTO_INCREMENT,
    `socNetUserId`     int unsigned NOT NULL,
    `socNetTypeId`     int unsigned NOT NULL,
    `create_tm`        int unsigned          DEFAULT NULL,
    `login_tm`         int unsigned          DEFAULT NULL,
    `logout_tm`        int unsigned          DEFAULT NULL,
    `nextPointId`      int unsigned NOT NULL DEFAULT '1',
    `fullRecoveryTime` int unsigned          DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `socNetUniqueKey` (`socNetUserId`, `socNetTypeId`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;