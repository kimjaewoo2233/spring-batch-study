create table person (
    id bigint primary key auto_increment,
    name varchar(255),
    age varchar(255),
    address varchar(255)
);

insert into person(name,age,address)
    values ('이경원','32','제주');
insert into person(name,age,address)
    values ('아무개','33','서울');
insert into person(name,age,address)
    values ('남궁성','34','인천');
