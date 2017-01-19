USE [BitCoinATpqACXAu]
GO

/****** Object:  Table [BitCoinBot].[gemini_price_btc_usd]    Script Date: 10/21/2015 5:27:21 AM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [BitCoinBot].[index_price_usd](
	[id] [nvarchar](255) NOT NULL,
	[price] [float] NULL,
	[server_time] [int] NULL,
PRIMARY KEY NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO

ALTER TABLE [BitCoinBot].[index_price_usd] ADD  CONSTRAINT [index_price_usd_id]  DEFAULT (CONVERT([nvarchar](255),newid(),(0))) FOR [id]
GO


