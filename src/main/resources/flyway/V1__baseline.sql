create table example
(
    example_id      bigint generated by default as identity primary key,
    example_property varchar(255) not null
);