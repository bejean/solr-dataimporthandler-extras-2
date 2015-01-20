#
# Table structure for table 'properties'
#
DROP TABLE IF EXISTS properties;
CREATE TABLE properties (
  scope                   char(32) NOT NULL,
  name					char(32) NOT NULL,  
  value					varchar(255),
  PRIMARY KEY (scope, name)
)
ENGINE InnoDB
DELAY_KEY_WRITE=1
MAX_ROWS=1000000 MIN_ROWS=10000
CHARACTER SET utf8 COLLATE utf8_general_ci;
;  
insert into properties (scope, name, value) values ('default', 'driver', 'com.mysql.jdbc.Driver');
insert into properties (scope, name, value) values ('dih', 'url', 'jdbc:mysql://localhost:3306/dih_data');
insert into properties (scope, name, value) values ('dih', 'user', 'solr');
insert into properties (scope, name, value) values ('dih', 'password', 'solr123');
insert into properties (scope, name, value) values ('dih', 'table', 'entry');
