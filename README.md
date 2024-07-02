# Stock Market Visualiser
## Aims
- Enhance understanding of financial markets
- Learn to build GUIs in Java and enhance understanding of UI frameworks
- Build on data visualisation skills
- Build robust, regular, and disciplined version control practices
- Moving beyond using the terminal as UI

## Tech Stack

- **Programming Language:** Java
- **Libraries and APIs:**
  - **JSoup:** For web scraping
  - **Gson:** For JSON parsing
  - **Polygon API:** For fetching stock data
  - **JavaFX:** For building the user interface
- **Tools:**
  - **Maven:** For project management and dependency management

## Key Classes Overview

### 1. DataProcessor

#### Description
The `DataProcessor` class is responsible for handling and processing raw stock data. It transforms raw inputs into a structured format suitable for visualisation and analysis.

#### Key Features
- Efficient data processing algorithms
- Data validation and error handling
- Integration with multiple data sources

### 2. EventHandlers

#### Description
The `EventHandlers` class manages all event-driven actions within the application. This includes user interactions, real-time updates, and automated triggers.

#### Key Features
- Robust event handling framework
- Asynchronous processing for real-time updates
- Seamless integration with UI components

### 3. IndexWikipediaScraper

#### Description
The `IndexWikipediaScraper` class uses JSoup to scrape stock index data from Wikipedia. This class ensures that you have up-to-date index information.

#### Key Features
- Reliable web scraping with JSoup
- Data extraction and parsing
- Automated data updates

### 4. NewsClient

#### Description
The `NewsClient` class interfaces with news APIs to fetch the latest news related to stock markets. This class provides context to market movements by presenting relevant news articles.

#### Key Features
- Integration with multiple news APIs
- Real-time news fetching
- Filtering and categorizing news articles

### 5. NewsDataParser

#### Description
The `NewsDataParser` class is responsible for parsing the news data fetched by the `NewsClient`. It extracts relevant information and structures it for easy access and analysis.

#### Key Features
- Efficient JSON parsing with Gson
- Error handling for data inconsistencies
- Structured data output

### 6. PolygonClient

#### Description
The `PolygonClient` class connects to the Polygon API to fetch stock data. This class ensures that the application has access to accurate and up-to-date market information.

#### Key Features
- Secure API connections
- Data fetching and caching
- Support for various market data types

### 7. StockChartFactory

#### Description
The `StockChartFactory` class generates visual representations of stock data. It uses JavaFX to create interactive and informative charts.

#### Key Features
- Customizable chart options
- Interactive data visualization
- High-performance rendering

### 8. StockDataParser

#### Description
The `StockDataParser` class processes and parses raw stock data into a format suitable for visualization. It handles different data formats and ensures consistency.

#### Key Features
- Robust data parsing algorithms
- Support for multiple data formats
- Data validation and error handling

### 9. UI

#### Description
The `UI` class is the main entry point for the application's user interface. It initializes and manages the overall layout and functionality of the UI.

#### Key Features
- Intuitive and user-friendly interface
- Integration with all application components
- Responsive design

### 10. UIComponents

#### Description
The `UIComponents` class provides reusable UI elements for the application. It includes components like buttons, charts, tables, and more.

#### Key Features
- Reusable and customisable UI components
- Consistent design language
- Easy integration with the main UI

