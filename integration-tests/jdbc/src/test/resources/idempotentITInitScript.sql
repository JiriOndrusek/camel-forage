CREATE TABLE camel_idempotent
(
    processorName VARCHAR(255),
    messageId     VARCHAR(100),
    createdAt     TIMESTAMP,
    PRIMARY KEY (processorName, messageId)
);