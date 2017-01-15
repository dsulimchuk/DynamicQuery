insert into services(id, name) values (1, 'test1');
insert into services(id, name) values (2, 'test2');
insert into services(id, name) values (3, 'test3');
insert into services(id, name) values (4, 'test4');
insert into services(id, name) values (5, 'test5');

insert into users(id, name, salary) values (1, 'user1', 10);
insert into users(id, name, salary) values (2, 'user2', 20);
insert into users(id, name, salary) values (3, 'user3', 30);
insert into users(id, name, salary) values (4, 'user4', 40);
insert into users(id, name, salary) values (5, 'user5', 50);

insert into users_services(users_id, services_id) values (1, 1);
insert into users_services(users_id, services_id) values (2, 2);
insert into users_services(users_id, services_id) values (3, 3);
insert into users_services(users_id, services_id) values (3, 2);
insert into users_services(users_id, services_id) values (4, 4);
