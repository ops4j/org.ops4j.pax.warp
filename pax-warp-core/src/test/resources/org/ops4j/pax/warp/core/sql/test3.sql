create table t (a int not null, b varchar(255));
insert into t (a, b) values (2, 'two'); -- end 
insert into t (a, b) values (3, 'three');
-- another comment
insert into t (a, b) values (3, 'three; yet again');

insert into t (a, b) values (6-2, 'four');
insert into t (a, b) values (5, 'two''s complement');
