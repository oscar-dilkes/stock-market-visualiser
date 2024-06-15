Get live data from alpha vantage by acquiring API key

Explore other existing stock market visualisers and determine required components

Construct class diagram

13/06/2024
Began to explore UI development using Swing.
Basic UI showing scroll list of high prices for a chosen stock implemented.
Alpha Vantage API used initially, although limited to 25 requests per day, so other options must be explored.

14/06/2024
No internet connection so implemented boolean flag in Stock object to say whether data retrieved successfully, to avoid null comparisons.

15/06/2024
Change over to yahoofinance API, but yahoo have stopped API use for stock data. Shame as it looked very straightforward etc. stock object provided with API.
Now change over to polygon.io API, 5 request per minute on free tier should be sufficient for personal use.
Next step: revert version to Alpha Vantage version as need to use custom stock object again and http requests.
Must remember to include some sort of wait and retry after ~ 1 minute if requests exceeded.