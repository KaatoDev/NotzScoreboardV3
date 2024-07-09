
create table if not exists scoreboardmodel(
    id integer primary key autoincrement,
    name varchar(36) unique not null,
    scoreboard blob not null);
    
create table if not exists playermodel(
    id integer primary key autoincrement,
    name varchar(36) unique not null,
    scoreboardname int not null,
    constraint scoreboardidfk foreign key (scoreboardname) references scoreboardmodel(name) on delete cascade)