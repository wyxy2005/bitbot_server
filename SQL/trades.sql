CREATE TABLE bitcoinbot.kraken_trades_xbt_eur
(
 [Id] INT IDENTITY NOT NULL PRIMARY KEY, 
    [price] FLOAT NOT NULL DEFAULT 0, 
    [amount] FLOAT NOT NULL DEFAULT 0, 
    [type] SMALLINT NOT NULL DEFAULT 0, 
    [LastPurchaseTime] INT NOT NULL
)