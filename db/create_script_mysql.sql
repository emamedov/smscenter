/*==============================================================*/
/* Table: channel                                               */
/*==============================================================*/
create table channel
(
   channel_id           tinyint unsigned not null auto_increment,
   name                 char(20) not null,
   host                 char(30) not null,
   port                 smallint unsigned not null,
   login                char(20) not null,
   password             char(30) not null,
   bind_type            char(3) not null,
   bind_ton             tinyint unsigned not null,
   bind_npi             tinyint unsigned not null,
   source_addr_ton      tinyint unsigned not null,
   source_addr_npi      tinyint unsigned not null,
   dest_addr_ton        tinyint unsigned not null,
   dest_addr_npi        tinyint unsigned not null,
   send_speed           smallint not null,
   check_speed          smallint not null,
   enabled              boolean not null,
   primary key (channel_id),
   unique key AK_Unique_Channel_Key (login, host, port)
);

/*==============================================================*/
/* Index: Channel_Index                                         */
/*==============================================================*/
create index Channel_Index on channel
(
   host
);

/*==============================================================*/
/* Table: channel2sender                                        */
/*==============================================================*/
create table channel2sender
(
   channel_id           tinyint unsigned not null,
   sender_id            smallint unsigned not null,
   primary key (channel_id, sender_id)
);

/*==============================================================*/
/* Table: sender                                                */
/*==============================================================*/
create table sender
(
   sender_id            smallint unsigned not null auto_increment,
   sign                 char(15) not null,
   primary key (sender_id),
   unique key AK_Unique_Sender_Sign_Key (sign)
);

/*==============================================================*/
/* Index: Sender_Index                                          */
/*==============================================================*/
create index Sender_Index on sender
(
   sign
);

/*==============================================================*/
/* Table: sms                                                   */
/*==============================================================*/
create table sms
(
   server_id            integer unsigned not null,
   phone                char(20) not null,
   sender_id            smallint unsigned,
   sender_sign          char(15),
   channel_id           tinyint unsigned,
   sms_text             char(160) not null,
   sms_group_id         integer unsigned not null,
   sms_group_count      tinyint unsigned not null,
   sms_group_index      tinyint unsigned not null,
   status               tinyint not null,
   user_id              smallint unsigned not null,
   smsclient_id         integer not null,
   date_client          datetime not null,
   date_server          datetime not null,
   last_updated_date    datetime not null,
   smsc_id              char(65),
   informed             boolean not null default 1,
   version              smallint unsigned not null default 0,
   primary key (server_id)
);

/*==============================================================*/
/* Index: SMS_Status_User_Index                                 */
/*==============================================================*/
create index SMS_Status_User_Index on sms
(
   status,
   user_id
);

/*==============================================================*/
/* Index: SMS_Date_Index                                        */
/*==============================================================*/
create index SMS_Date_Index on sms
(
   date_client
);

/*==============================================================*/
/* Index: SMS_Smsc_Index                                        */
/*==============================================================*/
create index SMS_Smsc_Index on sms
(
   smsc_id
);

/*==============================================================*/
/* Index: SMS_Status_Channel_Index                              */
/*==============================================================*/
create index SMS_Status_Channel_Index on sms
(
   status,
   channel_id
);

/*==============================================================*/
/* Table: user                                                  */
/*==============================================================*/
create table user
(
   user_id              smallint unsigned not null auto_increment,
   name                 char(10) not null,
   password             char(128) not null,
   allowed_ip           char(15),
   description          char(200) not null,
   created_on           datetime not null,
   last_login           datetime null,
   error_count          tinyint not null default 0,
   enabled              boolean not null default 1,
   primary key (user_id),
   unique key AK_Unique_User_Name_Key (name)
);

/*==============================================================*/
/* Index: User_Index                                            */
/*==============================================================*/
create unique index User_Index on user
(
   name,
   password
);

/*==============================================================*/
/* Table: user2sender                                           */
/*==============================================================*/
create table user2sender
(
   user_id              smallint unsigned not null,
   sender_id            smallint unsigned not null,
   primary key (user_id, sender_id)
);

alter table channel2sender add constraint FK_Channel_ID foreign key (channel_id)
      references channel (channel_id) on delete restrict on update restrict;

alter table channel2sender add constraint FK_Sender_ID2 foreign key (sender_id)
      references sender (sender_id) on delete restrict on update restrict;

alter table sms add constraint FK_SMS2Channel_ID foreign key (channel_id)
      references channel (channel_id) on delete restrict on update restrict;

alter table sms add constraint FK_SMS2Sender_ID foreign key (sender_id)
      references sender (sender_id) on delete restrict on update restrict;

alter table sms add constraint FK_SMS2User_ID foreign key (user_id)
      references user (user_id) on delete restrict on update restrict;

alter table user2sender add constraint FK_Sender_ID foreign key (sender_id)
      references sender (sender_id) on delete restrict on update restrict;

alter table user2sender add constraint FK_User_ID foreign key (user_id)
      references user (user_id) on delete restrict on update restrict;

