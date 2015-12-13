"use strict";
/*
	This class implements interaction with UDF-compatible datafeed.

	See UDF protocol reference at
	https://github.com/tradingview/charting_library/wiki/UDF
*/

var Datafeeds = {};

Datafeeds.UDFCompatibleDatafeed = function(datafeedURL, updateFrequency, protocolVersion) {

	this._datafeedURL = datafeedURL;
	this._configuration = undefined;

	this._symbolSearch = null;
	this._symbolsStorage = null;
	this._barsPulseUpdater = new Datafeeds.DataPulseUpdater(this, updateFrequency || 5 * 1000);
	this._quotesPulseUpdater = new Datafeeds.QuotesPulseUpdater(this);
	this._protocolVersion = protocolVersion || 2;

	this._enableLogging = false;
	this._initializationFinished = false;
	this._callbacks = {};

	this._initialize();
};

Datafeeds.UDFCompatibleDatafeed.prototype.defaultConfiguration = function() {
	return {
		supports_search: false,
		supports_group_request: true,
		supported_resolutions: ["1m", "3m", "5m", "15m", "30m", "60m", "120m", "240m", "360m", "720m", "1D", "2D", "3D", "W", "3W", "1M"],
		supports_marks: false,
		supports_timescale_marks: false
	};
};

Datafeeds.UDFCompatibleDatafeed.prototype.getServerTime = function(callback) {
	if (this._configuration.supports_time) {
		this._send(this._datafeedURL + "/time", {})
			.done(function (response) {
				callback(+response);
			})
			.fail(function() {

			});
	}
};

Datafeeds.UDFCompatibleDatafeed.prototype.on = function (event, callback) {

	if (!this._callbacks.hasOwnProperty(event)) {
		this._callbacks[event] = [];
	}

	this._callbacks[event].push(callback);
	return this;
};


Datafeeds.UDFCompatibleDatafeed.prototype._fireEvent = function(event, argument) {
	if (this._callbacks.hasOwnProperty(event)) {
		var callbacksChain = this._callbacks[event];
		for (var i = 0; i < callbacksChain.length; ++i) {
			callbacksChain[i](argument);
		}
		this._callbacks[event] = [];
	}
};

Datafeeds.UDFCompatibleDatafeed.prototype.onInitialized = function() {
	this._initializationFinished = true;
	this._fireEvent("initialized");
};



Datafeeds.UDFCompatibleDatafeed.prototype._logMessage = function(message) {
	if (this._enableLogging) {
		var now = new Date();
		console.log(now.toLocaleTimeString() + "." + now.getMilliseconds() + "> " + message);
	}
};


Datafeeds.UDFCompatibleDatafeed.prototype._send = function(url, params) {
	var request = url;
	if (params) {
		for (var i = 0; i < Object.keys(params).length; ++i) {
			var key = Object.keys(params)[i];
			var value = encodeURIComponent(params[key]);
			request += (i === 0 ? "?" : "&") + key + "=" + value;
		}
	}

	this._logMessage("New request: " + request);

	return $.ajax({
		type: 'GET',
		url: request,
		contentType: 'text/plain'
	});
};

Datafeeds.UDFCompatibleDatafeed.prototype._initialize = function() {

	var that = this;

	this._send(this._datafeedURL + "/config")
		.done(function(response) {
			var configurationData = JSON.parse(response);
			that._setupWithConfiguration(configurationData);
		})
		.fail(function(reason) {
			that._setupWithConfiguration(that.defaultConfiguration());
		});
};


Datafeeds.UDFCompatibleDatafeed.prototype.onReady = function(callback) {

	if (this._configuration) {
		callback(this._configuration);
	}
	else {
		var that = this;
		this.on("configuration_ready", function() {
			callback(that._configuration);
		});
	}
};

Datafeeds.UDFCompatibleDatafeed.prototype._setupWithConfiguration = function(configurationData) {
	this._configuration = configurationData;

	if (!configurationData.exchanges) {
		configurationData.exchanges = [];
	}

	//	@obsolete; remove in 1.5
	var supportedResolutions = configurationData.supported_resolutions || configurationData.supportedResolutions;
	configurationData.supported_resolutions = supportedResolutions;

	//	@obsolete; remove in 1.5
	var symbolsTypes = configurationData.symbols_types || configurationData.symbolsTypes;
	configurationData.symbols_types = symbolsTypes;

	if (!configurationData.supports_search && !configurationData.supports_group_request) {
		throw "Unsupported datafeed configuration. Must either support search, or support group request";
	}

	if (!configurationData.supports_search) {
		this._symbolSearch = new Datafeeds.SymbolSearchComponent(this);
	}

	if (configurationData.supports_group_request) {
		//	this component will call onInitialized() by itself
		this._symbolsStorage = new Datafeeds.SymbolsStorage(this);
	}
	else {
		this.onInitialized();
	}

	this._fireEvent("configuration_ready");
	this._logMessage("Initialized with " + JSON.stringify(configurationData));
};


//	===============================================================================================================================
//	The functions set below is the implementation of JavaScript API.

Datafeeds.UDFCompatibleDatafeed.prototype.getMarks = function (symbolInfo, rangeStart, rangeEnd, onDataCallback, resolution) {
	if (this._configuration.supports_marks) {
		this._send(this._datafeedURL + "/marks", {
				symbol: symbolInfo.ticker.toUpperCase(),
				from : rangeStart,
				to: rangeEnd,
				resolution: resolution
			})
			.done(function (response) {
				onDataCallback(JSON.parse(response));
			})
			.fail(function() {
				onDataCallback([]);
			});
	}
};

Datafeeds.UDFCompatibleDatafeed.prototype.getTimescaleMarks = function (symbolInfo, rangeStart, rangeEnd, onDataCallback, resolution) {
	if (this._configuration.supports_timescale_marks) {
		this._send(this._datafeedURL + "/timescale_marks", {
			symbol: symbolInfo.ticker.toUpperCase(),
			from : rangeStart,
			to: rangeEnd,
			resolution: resolution
		})
			.done(function (response) {
				onDataCallback(JSON.parse(response));
			})
			.fail(function() {
				onDataCallback([]);
			});
	}
};

Datafeeds.UDFCompatibleDatafeed.prototype.searchSymbolsByName = function(ticker, exchange, type, onResultReadyCallback) {
	var MAX_SEARCH_RESULTS = 30;

	if (!this._configuration) {
		onResultReadyCallback([]);
		return;
	}

	if (this._configuration.supports_search) {

		this._send(this._datafeedURL + "/search", {
				limit: MAX_SEARCH_RESULTS,
				query: ticker.toUpperCase(),
				type: type,
				exchange: exchange
			})
			.done(function (response) {
				var data = JSON.parse(response);

				for (var i = 0; i < data.length; ++i) {
					if (!data[i].params) {
						data[i].params = [];
					}
				}

				if (typeof data.s == "undefined" || data.s != "error") {
					onResultReadyCallback(data);
				}
				else {
					onResultReadyCallback([]);
				}

			})
			.fail(function(reason) {
				onResultReadyCallback([]);
			});
	}
	else {

		if (!this._symbolSearch) {
			throw "Datafeed error: inconsistent configuration (symbol search)";
		}

		var searchArgument = {
			ticker: ticker,
			exchange: exchange,
			type: type,
			onResultReadyCallback: onResultReadyCallback
		};

		if (this._initializationFinished) {
			this._symbolSearch.searchSymbolsByName(searchArgument, MAX_SEARCH_RESULTS);
		}
		else {

			var that = this;

			this.on("initialized", function() {
				that._symbolSearch.searchSymbolsByName(searchArgument, MAX_SEARCH_RESULTS);
			});
		}
	}
};


Datafeeds.UDFCompatibleDatafeed.prototype._symbolResolveURL = "/symbols";


//	BEWARE: this function does not consider symbol's exchange
Datafeeds.UDFCompatibleDatafeed.prototype.resolveSymbol = function(symbolName, onSymbolResolvedCallback, onResolveErrorCallback) {

	var that = this;

	if (!this._initializationFinished) {
		this.on("initialized", function() {
			that.resolveSymbol(symbolName, onSymbolResolvedCallback, onResolveErrorCallback);
		});

		return;
	}

	var resolveRequestStartTime = Date.now();
	that._logMessage("Resolve requested");

	function onResultReady(data) {
		var postProcessedData = data;
		if (that.postProcessSymbolInfo) {
			postProcessedData = that.postProcessSymbolInfo(postProcessedData);
		}

		that._logMessage("Symbol resolved: " + (Date.now() - resolveRequestStartTime));

		onSymbolResolvedCallback(postProcessedData);
	}

	if (!this._configuration.supports_group_request) {
		this._send(this._datafeedURL + this._symbolResolveURL, {
				symbol: symbolName ? symbolName.toUpperCase() : ""
			})
			.done(function (response) {
				var data = JSON.parse(response);

				if (data.s && data.s != "ok") {
					onResolveErrorCallback("unknown_symbol");
				}
				else {
					onResultReady(data);
				}
			})
			.fail(function(reason) {
				that._logMessage("Error resolving symbol: " + JSON.stringify([reason]));
				onResolveErrorCallback("unknown_symbol");
			});
	}
	else {
		if (this._initializationFinished) {
			this._symbolsStorage.resolveSymbol(symbolName, onResultReady, onResolveErrorCallback);
		}
		else {
			this.on("initialized", function() {
				that._symbolsStorage.resolveSymbol(symbolName, onResultReady, onResolveErrorCallback);
			});
		}
	}
};


Datafeeds.UDFCompatibleDatafeed.prototype._historyURL = "/history";

Datafeeds.UDFCompatibleDatafeed.prototype.getBars = function(symbolInfo, resolution, rangeStartDate, rangeEndDate, onDataCallback, onErrorCallback) {

	//	timestamp sample: 1399939200
	if (rangeStartDate > 0 && (rangeStartDate + "").length > 10) {
		throw ["Got a JS time instead of Unix one.", rangeStartDate, rangeEndDate];
	}

	var that = this;

	var requestStartTime = Date.now();
	var nonce = Math.floor(requestStartTime / 1000);
	
	var reqsymbol = symbolInfo.ticker.toUpperCase();
	this._send(this._datafeedURL + this._historyURL, {
			symbol: reqsymbol,
			resolution: resolution,
			from: rangeStartDate,
			to: rangeEndDate,
			nonce: nonce,
			hash: SHA256(reqsymbol + (rangeStartDate & rangeEndDate) + resolution + nonce)
	})
	.done(function (response) {

		var data = JSON.parse(response);

		var nodata = data.s == "no_data";

		if (data.s != "ok" && !nodata) {
			if (!!onErrorCallback) {
				onErrorCallback(data.s);
			}
			return;
		}

		var bars = [];

		//	data is JSON having format {s: "status" (ok, no_data, error),
		//  v: [volumes], t: [times], o: [opens], h: [highs], l: [lows], c:[closes], nb: "optional_unixtime_if_no_data"}
		var barsCount = nodata ? 0 : data.t.length;

		var volumePresent = typeof data.v != "undefined";
		var ohlPresent = typeof data.o != "undefined";

		for (var i = 0; i < barsCount; ++i) {

			var barValue = {
				time: data.t[i] * 1000,
				close: data.c[i]
			};

			if (ohlPresent) {
				barValue.open = data.o[i];
				barValue.high = data.h[i];
				barValue.low = data.l[i];
			}
			else {
				barValue.open = barValue.high = barValue.low = barValue.close;
			}

			if (volumePresent) {
				barValue.volume = data.v[i];
			}

			bars.push(barValue);
		}
		
		// Set page title
		if (barsCount > 0) {
			document.title = reqsymbol + " " + data.c[barsCount-1];
		}
		
		onDataCallback(bars, {version: that._protocolVersion, noData: nodata, nextTime: data.nb || data.nextTime});
	})
	.fail(function (arg) {
		console.warn(["getBars(): HTTP error", arg]);

		if (!!onErrorCallback) {
			onErrorCallback("network error: " + JSON.stringify(arg));
		}
	});
};


Datafeeds.UDFCompatibleDatafeed.prototype.subscribeBars = function(symbolInfo, resolution, onRealtimeCallback, listenerGUID) {
	this._barsPulseUpdater.subscribeDataListener(symbolInfo, resolution, onRealtimeCallback, listenerGUID);
};

Datafeeds.UDFCompatibleDatafeed.prototype.unsubscribeBars = function(listenerGUID) {
	this._barsPulseUpdater.unsubscribeDataListener(listenerGUID);
};

Datafeeds.UDFCompatibleDatafeed.prototype.calculateHistoryDepth = function(period, resolutionBack, intervalBack) {
};

Datafeeds.UDFCompatibleDatafeed.prototype.getQuotes = function(symbols, onDataCallback, onErrorCallback) {
	this._send(this._datafeedURL + "/quotes", { symbols: symbols })
		.done(function (response) {
			var data = JSON.parse(response);
			if (data.s == "ok") {
				//	JSON format is {s: "status", [{s: "symbol_status", n: "symbol_name", v: {"field1": "value1", "field2": "value2", ..., "fieldN": "valueN"}}]}
				if (onDataCallback) {  
					onDataCallback(data.d);  
					}  
			} else {
				if (onErrorCallback) {  
					onErrorCallback(data.errmsg);  
					}  
			}
		})
		.fail(function (arg) {
			if (onErrorCallback) { 
				onErrorCallback("network error: " + arg); 
			}
		});
};

Datafeeds.UDFCompatibleDatafeed.prototype.subscribeQuotes = function(symbols, fastSymbols, onRealtimeCallback, listenerGUID) {
	this._quotesPulseUpdater.subscribeDataListener(symbols, fastSymbols, onRealtimeCallback, listenerGUID);
};

Datafeeds.UDFCompatibleDatafeed.prototype.unsubscribeQuotes = function(listenerGUID) {
	this._quotesPulseUpdater.unsubscribeDataListener(listenerGUID);
};

//	==================================================================================================================================================
//	==================================================================================================================================================
//	==================================================================================================================================================

/*
	It's a symbol storage component for ExternalDatafeed. This component can
	  * interact to UDF-compatible datafeed which supports whole group info requesting
	  * do symbol resolving -- return symbol info by its name
*/
Datafeeds.SymbolsStorage = function(datafeed) {
	this._datafeed = datafeed;

	this._exchangesList = ["All Exchanges",    "BTCe", "Bitfinex", "Bitstamp", "Okcoin", "BTCChina", "Coinbase", "CoinbaseExchange", "Campbx", "Itbit", "Cryptsy", "796", "Fybsg", "Fybse", "Kraken", "CexIO", "Dgex"];
	this._exchangesWaitingForData = {};
	this._exchangesDataCache = {};

	this._symbolsInfo = {};
	this._symbolsList = [];

	this._requestFullSymbolsList();
};



Datafeeds.SymbolsStorage.prototype._requestFullSymbolsList = function() {

	var that = this;
	var datafeed = this._datafeed;

	for (var i = 0; i < this._exchangesList.length; ++i) {

		var exchange = this._exchangesList[i];

		if (this._exchangesDataCache.hasOwnProperty(exchange)) {
			continue;
		}

		this._exchangesDataCache[exchange] = true;

		this._exchangesWaitingForData[exchange] = "waiting_for_data";

		this._datafeed._send(this._datafeed._datafeedURL + "/symbol_info", {
				group: exchange
			})
			.done(function(exchange) {
				return function(response) {
					that._onExchangeDataReceived(exchange, JSON.parse(response));
					that._onAnyExchangeResponseReceived(exchange);
				};
			}(exchange)) //jshint ignore:line 
			.fail(function(exchange) {
				return function (reason) {
					that._onAnyExchangeResponseReceived(exchange);
				};
			}(exchange)); //jshint ignore:line 
	}
};

Datafeeds.SymbolsStorage.prototype._onExchangeDataReceived = function(exchangeName, data) {

	function tableField(data, name, index) {
		return data[name] instanceof Array ? 
			data[name][index] : 
			data[name]; 
	}

	try
	{
		for (var symbolIndex = 0; symbolIndex < data.symbol.length; ++symbolIndex) {

			var symbolName = data.symbol[symbolIndex];
			var listedExchange = tableField(data, "exchange-listed", symbolIndex);
			var tradedExchange = tableField(data, "exchange-traded", symbolIndex);
			var fullName = tradedExchange + ":" + symbolName;

			//	This feature support is not implemented yet
			//	var hasDWM = tableField(data, "has-dwm", symbolIndex);

			var hasIntraday = tableField(data, "has-intraday", symbolIndex);

			var tickerPresent = typeof data.ticker != "undefined"; 

			var symbolInfo = {
				name: symbolName,
				base_name: [listedExchange + ":" + symbolName],
				description: tableField(data, "description", symbolIndex),
				full_name: fullName,
				legs: [fullName],
				has_intraday: hasIntraday,
				has_no_volume: tableField(data, "has-no-volume", symbolIndex),
				listed_exchange: listedExchange,
				exchange: tradedExchange,
				minmov: tableField(data, "minmovement", symbolIndex) || tableField(data, "minmov", symbolIndex) ,
				minmove2: tableField(data, "minmove2", symbolIndex) || tableField(data, "minmov2", symbolIndex) ,
				fractional: tableField(data, "fractional", symbolIndex),
				pointvalue: tableField(data, "pointvalue", symbolIndex),
				pricescale: tableField(data, "pricescale", symbolIndex),
				type: tableField(data, "type", symbolIndex),
				session: tableField(data, "session-regular", symbolIndex),
				ticker: tickerPresent ? tableField(data, "ticker", symbolIndex) : symbolName,
				timezone: tableField(data, "timezone", symbolIndex),
				supported_resolutions: tableField(data, "supported-resolutions", symbolIndex) || this._datafeed.defaultConfiguration().supported_resolutions,
				force_session_rebuild: tableField(data, "force-session-rebuild", symbolIndex) || false,
				has_daily: tableField(data, "has-daily", symbolIndex) || true,
				intraday_multipliers: tableField(data, "intraday-multipliers", symbolIndex) || ["1", "5", "15", "30", "60"],
				has_fractional_volume: tableField(data, "has-fractional-volume", symbolIndex) || false,
				has_weekly_and_monthly: tableField(data, "has-weekly-and-monthly", symbolIndex) || false,
				has_empty_bars: tableField(data, "has-empty-bars", symbolIndex) || false,
				volume_precision: tableField(data, "volume-precision", symbolIndex) || 0
			};

			this._symbolsInfo[symbolInfo.ticker] = this._symbolsInfo[symbolName] = this._symbolsInfo[fullName] = symbolInfo;
			this._symbolsList.push(symbolName);
		}
	}
	catch (error) {
		throw "API error when processing exchange `" + exchangeName + "` symbol #" + symbolIndex + ": " + error;
	}
};


Datafeeds.SymbolsStorage.prototype._onAnyExchangeResponseReceived = function(exchangeName) {

	delete this._exchangesWaitingForData[exchangeName];

	var allDataReady = Object.keys(this._exchangesWaitingForData).length === 0;

	if (allDataReady) {
		this._symbolsList.sort();
		this._datafeed._logMessage("All exchanges data ready");
		this._datafeed.onInitialized();
	}
};


//	BEWARE: this function does not consider symbol's exchange
Datafeeds.SymbolsStorage.prototype.resolveSymbol = function(symbolName, onSymbolResolvedCallback, onResolveErrorCallback) {

	if (!this._symbolsInfo.hasOwnProperty(symbolName)) {
		onResolveErrorCallback("invalid symbol");
	}
	else {
		onSymbolResolvedCallback(this._symbolsInfo[symbolName]);
	}

};


//	==================================================================================================================================================
//	==================================================================================================================================================
//	==================================================================================================================================================

/*
	It's a symbol search component for ExternalDatafeed. This component can do symbol search only.
	This component strongly depends on SymbolsDataStorage and cannot work without it. Maybe, it would be
	better to merge it to SymbolsDataStorage.
*/

Datafeeds.SymbolSearchComponent = function(datafeed) {
	this._datafeed = datafeed;
};



//	searchArgument = { ticker, onResultReadyCallback}
Datafeeds.SymbolSearchComponent.prototype.searchSymbolsByName = function(searchArgument, maxSearchResults) {

	if (!this._datafeed._symbolsStorage) {
		throw "Cannot use local symbol search when no groups information is available";
	}

	var symbolsStorage = this._datafeed._symbolsStorage;

	var results = [];
	var queryIsEmpty = !searchArgument.ticker || searchArgument.ticker.length === 0;

	for (var i = 0; i < symbolsStorage._symbolsList.length; ++i) {
		var symbolName = symbolsStorage._symbolsList[i];
		var item = symbolsStorage._symbolsInfo[symbolName];

		if (searchArgument.type && searchArgument.type.length > 0 && item.type != searchArgument.type) {
			continue;
		}
		if (searchArgument.exchange && searchArgument.exchange.length > 0 && item.exchange != searchArgument.exchange) {
			continue;
		}
		if (queryIsEmpty || item.name.indexOf(searchArgument.ticker) === 0) {
			results.push({
				symbol: item.name,
				full_name: item.full_name,
				description: item.description,
				exchange: item.exchange,
				params: [],
				type: item.type,
				ticker: item.name
			});
		}

		if (results.length >= maxSearchResults) {
			break;
		}
	}

	searchArgument.onResultReadyCallback(results);
};



//	==================================================================================================================================================
//	==================================================================================================================================================
//	==================================================================================================================================================

/*
	This is a pulse updating components for ExternalDatafeed. They emulates realtime updates with periodic requests.
*/

Datafeeds.DataPulseUpdater = function(datafeed, updateFrequency) {
	this._datafeed = datafeed;
	this._subscribers = {};

	this._requestsPending = 0;
	var that = this;

	var update = function() {
		if (that._requestsPending > 0) {
			return;
		}

		for (var listenerGUID in that._subscribers) {
			var subscriptionRecord = that._subscribers[listenerGUID];
			var resolution = subscriptionRecord.resolution;

			var datesRangeRight = parseInt((new Date().valueOf()) / 1000);

			//	BEWARE: please note we really need 2 bars, not the only last one
			//	see the explanation below. `10` is the `large enough` value to work around holidays
			var datesRangeLeft = datesRangeRight - that.periodLengthSeconds(resolution, 10);

			that._requestsPending++;

			(function(_subscriptionRecord) {

				that._datafeed.getBars(_subscriptionRecord.symbolInfo, resolution, datesRangeLeft, datesRangeRight, function(bars) {
					that._requestsPending--;

					//	means the subscription was cancelled while waiting for data
					if (!that._subscribers.hasOwnProperty(listenerGUID)) {
						return;
					}

					if (bars.length === 0) {
						return;
					}

					var lastBar = bars[bars.length - 1];
					if (!isNaN(_subscriptionRecord.lastBarTime) && lastBar.time < _subscriptionRecord.lastBarTime) {
						return;
					}

					var subscribers = _subscriptionRecord.listeners;

					//	BEWARE: this one isn't working when first update comes and this update makes a new bar. In this case
					//	_subscriptionRecord.lastBarTime = NaN
					var isNewBar = !isNaN(_subscriptionRecord.lastBarTime) && lastBar.time > _subscriptionRecord.lastBarTime;

					//	Pulse updating may miss some trades data (ie, if pulse period = 10 secods and new bar is started 5 seconds later after the last update, the
					//	old bar's last 5 seconds trades will be lost). Thus, at fist we should broadcast old bar updates when it's ready.
					if (isNewBar) {

						if (bars.length < 2) {
							throw "Not enough bars in history for proper pulse update. Need at least 2.";
						}

						var previousBar = bars[bars.length - 2];
						for (var i =0; i < subscribers.length; ++i) {
							subscribers[i](previousBar);
						}
					}

					_subscriptionRecord.lastBarTime = lastBar.time;

					for (var i =0; i < subscribers.length; ++i) {
						subscribers[i](lastBar);
					}
				},

				//	on error
				function() {
					that._requestsPending--;
				});
			})(subscriptionRecord); //jshint ignore:line 

		}
	}

	if (typeof updateFrequency != "undefined" && updateFrequency > 0) {
		setInterval(update, updateFrequency);
	}
};


Datafeeds.DataPulseUpdater.prototype.unsubscribeDataListener = function(listenerGUID) {
	this._datafeed._logMessage("Unsubscribing " + listenerGUID);
	delete this._subscribers[listenerGUID];
};


Datafeeds.DataPulseUpdater.prototype.subscribeDataListener = function(symbolInfo, resolution, newDataCallback, listenerGUID) {

	this._datafeed._logMessage("Subscribing " + listenerGUID);

	var key = symbolInfo.name + ", " + resolution;

	if (!this._subscribers.hasOwnProperty(listenerGUID)) {

		this._subscribers[listenerGUID] = {
			symbolInfo: symbolInfo,
			resolution: resolution,
			lastBarTime: NaN,
			listeners: []
		};
	}

	this._subscribers[listenerGUID].listeners.push(newDataCallback);
};


Datafeeds.DataPulseUpdater.prototype.periodLengthSeconds = function(resolution, requiredPeriodsCount) {
	var daysCount = 0;

	if (resolution == "D") {
		daysCount = requiredPeriodsCount;
	}
	else if (resolution == "M") {
		daysCount = 31 * requiredPeriodsCount;
	}
	else if (resolution == "W") {
		daysCount = 7 * requiredPeriodsCount;
	}
	else {
		daysCount = requiredPeriodsCount * resolution / (24 * 60);
	}

	return daysCount * 24 * 60 * 60;
};


Datafeeds.QuotesPulseUpdater = function(datafeed) {
	this._datafeed = datafeed;
	this._subscribers = {};
	this._updateInterval = 60 * 1000;
	this._fastUpdateInterval = 10 * 1000;
	this._requestsPending = 0;

	var that = this;

	setInterval(function() {
		that._updateQuotes(function(subscriptionRecord) { return subscriptionRecord.symbols; });
	}, this._updateInterval);

	setInterval(function() {
		that._updateQuotes(function(subscriptionRecord) { return subscriptionRecord.fastSymbols.length > 0 ? subscriptionRecord.fastSymbols : subscriptionRecord.symbols; });
	}, this._fastUpdateInterval);
};

Datafeeds.QuotesPulseUpdater.prototype.subscribeDataListener = function(symbols, fastSymbols, newDataCallback, listenerGUID) {
	if (!this._subscribers.hasOwnProperty(listenerGUID)) {
		this._subscribers[listenerGUID] = {
			symbols: symbols,
			fastSymbols: fastSymbols,
			listeners: []
		};
	}
	this._subscribers[listenerGUID].listeners.push(newDataCallback);
};

Datafeeds.QuotesPulseUpdater.prototype.unsubscribeDataListener = function(listenerGUID) {
	delete this._subscribers[listenerGUID];
};

Datafeeds.QuotesPulseUpdater.prototype._updateQuotes = function(symbolsGetter) {
	if (this._requestsPending > 0) {
		return;
	}

	var that = this;
	for (var listenerGUID in this._subscribers) {
		this._requestsPending++;

		var subscriptionRecord = this._subscribers[listenerGUID];
		this._datafeed.getQuotes(symbolsGetter(subscriptionRecord),
			// onDataCallback
			function(subscribers, guid) {
				return function(data) {
					that._requestsPending--;

					// means the subscription was cancelled while waiting for data
					if (!that._subscribers.hasOwnProperty(guid)) {
						return;
					}

					for (var i =0; i < subscribers.length; ++i) {
						subscribers[i](data);
					}
				};
			}(subscriptionRecord.listeners, listenerGUID), //jshint ignore:line
			// onErrorCallback
			function (error) {
				that._requestsPending--;
			}); //jshint ignore:line 
	}
};




/**
*  Secure Hash Algorithm (SHA256)
*  http://www.webtoolkit.info/
*  Original code by Angel Marin, Paul Johnston
**/

function SHA256(s){
  var chrsz   = 8;
  var hexcase = 0;

 function safe_add (x, y) {
   var lsw = (x & 0xFFFF) + (y & 0xFFFF);
   var msw = (x >> 16) + (y >> 16) + (lsw >> 16);
   return (msw << 16) | (lsw & 0xFFFF);
 }

 function S (X, n) { return ( X >>> n ) | (X << (32 - n)); }
 function R (X, n) { return ( X >>> n ); }
 function Ch(x, y, z) { return ((x & y) ^ ((~x) & z)); }
 function Maj(x, y, z) { return ((x & y) ^ (x & z) ^ (y & z)); }
 function Sigma0256(x) { return (S(x, 2) ^ S(x, 13) ^ S(x, 22)); }
 function Sigma1256(x) { return (S(x, 6) ^ S(x, 11) ^ S(x, 25)); }
 function Gamma0256(x) { return (S(x, 7) ^ S(x, 18) ^ R(x, 3)); }
 function Gamma1256(x) { return (S(x, 17) ^ S(x, 19) ^ R(x, 10)); }

 function core_sha256 (m, l) {
   var K = new Array(0x428A2F98, 0x71374491, 0xB5C0FBCF, 0xE9B5DBA5, 0x3956C25B, 0x59F111F1, 0x923F82A4, 0xAB1C5ED5, 0xD807AA98, 0x12835B01, 0x243185BE, 0x550C7DC3, 0x72BE5D74, 0x80DEB1FE, 0x9BDC06A7, 0xC19BF174, 0xE49B69C1, 0xEFBE4786, 0xFC19DC6, 0x240CA1CC, 0x2DE92C6F, 0x4A7484AA, 0x5CB0A9DC, 0x76F988DA, 0x983E5152, 0xA831C66D, 0xB00327C8, 0xBF597FC7, 0xC6E00BF3, 0xD5A79147, 0x6CA6351, 0x14292967, 0x27B70A85, 0x2E1B2138, 0x4D2C6DFC, 0x53380D13, 0x650A7354, 0x766A0ABB, 0x81C2C92E, 0x92722C85, 0xA2BFE8A1, 0xA81A664B, 0xC24B8B70, 0xC76C51A3, 0xD192E819, 0xD6990624, 0xF40E3585, 0x106AA070, 0x19A4C116, 0x1E376C08, 0x2748774C, 0x34B0BCB5, 0x391C0CB3, 0x4ED8AA4A, 0x5B9CCA4F, 0x682E6FF3, 0x748F82EE, 0x78A5636F, 0x84C87814, 0x8CC70208, 0x90BEFFFA, 0xA4506CEB, 0xBEF9A3F7, 0xC67178F2);
   var HASH = new Array(0x6A09E667, 0xBB67AE85, 0x3C6EF372, 0xA54FF53A, 0x510E527F, 0x9B05688C, 0x1F83D9AB, 0x5BE0CD19);
   var W = new Array(64);
   var a, b, c, d, e, f, g, h, i, j;
   var T1, T2;

   m[l >> 5] |= 0x80 << (24 - l % 32);
   m[((l + 64 >> 9) << 4) + 15] = l;

   for ( var i = 0; i<m.length; i+=16 ) {
     a = HASH[0];
     b = HASH[1];
     c = HASH[2];
     d = HASH[3];
     e = HASH[4];
     f = HASH[5];
     g = HASH[6];
     h = HASH[7];

     for ( var j = 0; j<64; j++) {
       if (j < 16) W[j] = m[j + i];
       else W[j] = safe_add(safe_add(safe_add(Gamma1256(W[j - 2]), W[j - 7]), Gamma0256(W[j - 15])), W[j - 16]);

       T1 = safe_add(safe_add(safe_add(safe_add(h, Sigma1256(e)), Ch(e, f, g)), K[j]), W[j]);
       T2 = safe_add(Sigma0256(a), Maj(a, b, c));

       h = g;
       g = f;
       f = e;
       e = safe_add(d, T1);
       d = c;
       c = b;
       b = a;
       a = safe_add(T1, T2);
     }

     HASH[0] = safe_add(a, HASH[0]);
     HASH[1] = safe_add(b, HASH[1]);
     HASH[2] = safe_add(c, HASH[2]);
     HASH[3] = safe_add(d, HASH[3]);
     HASH[4] = safe_add(e, HASH[4]);
     HASH[5] = safe_add(f, HASH[5]);
     HASH[6] = safe_add(g, HASH[6]);
     HASH[7] = safe_add(h, HASH[7]);
   }
   return HASH;
 }

 function str2binb (str) {
   var bin = Array();
   var mask = (1 << chrsz) - 1;
   for(var i = 0; i < str.length * chrsz; i += chrsz) {
     bin[i>>5] |= (str.charCodeAt(i / chrsz) & mask) << (24 - i%32);
   }
   return bin;
 }

 function Utf8Encode(string) {
   string = string.replace(/\r\n/g,"\n");
   var utftext = "";

   for (var n = 0; n < string.length; n++) {

     var c = string.charCodeAt(n);

     if (c < 128) {
       utftext += String.fromCharCode(c);
     }
     else if((c > 127) && (c < 2048)) {
       utftext += String.fromCharCode((c >> 6) | 192);
       utftext += String.fromCharCode((c & 63) | 128);
     }
     else {
       utftext += String.fromCharCode((c >> 12) | 224);
       utftext += String.fromCharCode(((c >> 6) & 63) | 128);
       utftext += String.fromCharCode((c & 63) | 128);
     }

   }

   return utftext;
 }

 function binb2hex (binarray) {
   var hex_tab = hexcase ? "0123456789ABCDEF" : "0123456789abcdef";
   var str = "";
   for(var i = 0; i < binarray.length * 4; i++) {
     str += hex_tab.charAt((binarray[i>>2] >> ((3 - i%4)*8+4)) & 0xF) +
     hex_tab.charAt((binarray[i>>2] >> ((3 - i%4)*8  )) & 0xF);
   }
   return str;
 }

 s = Utf8Encode(s);
 return binb2hex(core_sha256(str2binb(s), s.length * chrsz));
}