
INSERT INTO roles (name)
VALUES ('USER');

INSERT INTO roles (name)
VALUES ('ROLE_PRE_AUTHENTICATED');


INSERT INTO users (username, password, two_factor_method, secret)
VALUES ('user', '$2a$10$0eR/4DEBxJJEw5YLwbYEvOtaKk6ziVWJICIYvW5ewa5h4uz4gTjg6','OTP', 'JSCKPSQY7KHKTND7');

INSERT INTO users (username, password, two_factor_method)
VALUES ('test', '$2a$10$0eR/4DEBxJJEw5YLwbYEvOtaKk6ziVWJICIYvW5ewa5h4uz4gTjg6','OTP');

INSERT INTO user_roles (user_id,role_id)
VALUES (1,1);

INSERT INTO user_roles (user_id,role_id)
VALUES (2,1);