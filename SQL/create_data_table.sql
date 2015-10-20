USE [BitCoinATpqACXAu]
GO

/****** Object:  Table [BitCoinBot].[gemini_price_btc_usd]    Script Date: 10/21/2015 5:27:21 AM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [BitCoinBot].[gemini_price_btc_usd](
	[id] [nvarchar](255) NOT NULL,
	[high] [float] NULL,
	[low] [float] NULL,
	[open] [float] NULL,
	[vol] [float] NULL,
	[vol_cur] [float] NULL,
	[server_time] [int] NULL,
	[close] [float] NULL,
	[buysell_ratio] [float] NULL,
PRIMARY KEY NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO

ALTER TABLE [BitCoinBot].[gemini_price_btc_usd] ADD  CONSTRAINT [DF_gemini_price_btc_usd_id]  DEFAULT (CONVERT([nvarchar](255),newid(),(0))) FOR [id]
GO


