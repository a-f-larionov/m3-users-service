CREATE TABLE IF NOT EXISTS `user_agents`
(
    `id`    int(11) unsigned NOT NULL AUTO_INCREMENT,
    `uid`   int(11)          NOT NULL,
    `agent` varchar(512)     NOT NULL DEFAULT '',
    PRIMARY KEY (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8;