# Neo4j Japanese Analyzer

A Neo4j plugin that provides Japanese text analysis capabilities using Apache Lucene's Kuromoji tokenizer. This analyzer enables full-text search for Japanese content in Neo4j databases with proper morphological analysis, stop word filtering, and tokenization.

## Features

- **Japanese Morphological Analysis**: Uses Kuromoji tokenizer for accurate Japanese text segmentation
- **Stop Words Filtering**: Removes common Japanese particles, auxiliary verbs, and punctuation
- **Case Normalization**: Converts text to uppercase for consistent searching
- **Mixed Language Support**: Handles both Japanese and English text in the same documents
- **Neo4j 5.x Compatible**: Built for Neo4j 5.x with proper service loading

## Project Structure

```
neo4j-japanese-analyzer/
├── .devcontainer/              # VSCode development container configuration
│   ├── devcontainer.json
│   └── Dockerfile
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/neo4j/analyzer/
│   │   │       └── JapaneseAnalyzerProvider.java
│   │   └── resources/
│   │       ├── META-INF/services/
│   │       │   └── org.neo4j.graphdb.schema.AnalyzerProvider
│   │       └── japanese-stopwords.txt
│   └── test/
│       └── java/
│           └── com/example/neo4j/analyzer/
│               └── JapaneseAnalyzerProviderTest.java
├── pom.xml
├── README.md
├── .gitignore
└── LICENSE
```

## Development Setup

### Using VSCode DevContainer (Recommended)

1. Open the project in VSCode
2. Install the "Dev Containers" extension
3. Press `Ctrl+Shift+P` and select "Dev Containers: Reopen in Container"
4. The container will build automatically with all required dependencies

### Manual Setup

Requirements:
- Java 21 or higher
- Maven 3.9.10 or higher

```bash
# Clone the repository
git clone https://github.com/your-username/neo4j-japanese-analyzer.git
cd neo4j-japanese-analyzer

# Compile the project
mvn clean compile

# Run tests
mvn test

# Build the plugin JAR
mvn clean package
```

## Building and Installation

### Build the Plugin

```bash
mvn clean package
```

This creates a JAR file in the `target/` directory: `neo4j-japanese-analyzer-1.0.0-SNAPSHOT.jar`

### Install in Neo4j

1. Copy the JAR file to your Neo4j `plugins/` directory:
   ```bash
   cp target/neo4j-japanese-analyzer-1.0.0-SNAPSHOT.jar /path/to/neo4j/plugins/
   ```

2. Restart your Neo4j instance

3. Verify the analyzer is available:
   ```cypher
   CALL db.index.fulltext.listAvailableAnalyzers()
   ```
   
   You should see `japanese` in the list of available analyzers.

## Usage

### Creating a Full-Text Index with Japanese Analyzer

```cypher
// Create a full-text index for Japanese content
CREATE FULLTEXT INDEX japanese_content_index 
FOR (n:Article) ON EACH [n.title, n.content] 
OPTIONS {indexConfig: {`fulltext.analyzer`: 'japanese'}}
```

### Searching Japanese Text

```cypher
// Insert some Japanese content
CREATE (a1:Article {title: '日本の技術', content: '日本は先進的な技術を持っています。'})
CREATE (a2:Article {title: 'コンピュータサイエンス', content: 'プログラミングは面白い分野です。'})

// Search for Japanese terms
CALL db.index.fulltext.queryNodes('japanese_content_index', '技術') 
YIELD node, score 
RETURN node.title, node.content, score 
ORDER BY score DESC
```

### Mixed Japanese and English Content

```cypher
// The analyzer handles mixed content well
CREATE (doc:Document {content: 'Neo4jは素晴らしいgraph databaseです。'})

// Search works for both Japanese and English terms
CALL db.index.fulltext.queryNodes('japanese_content_index', 'neo4j') 
YIELD node, score 
RETURN node.content, score

CALL db.index.fulltext.queryNodes('japanese_content_index', '素晴らしい') 
YIELD node, score 
RETURN node.content, score
```

## Testing

Run the test suite to verify functionality:

```bash
mvn test
```

The tests cover:
- Analyzer registration and availability
- Full-text index creation with Japanese analyzer
- Japanese text tokenization and search
- Stop words filtering
- Mixed Japanese and English text handling

## Configuration

### Stop Words

The analyzer uses a curated list of Japanese stop words located in `src/main/resources/japanese-stopwords.txt`. This includes:

- Particles (は、が、を、に、で、と、etc.)
- Auxiliary verbs and copula (だ、である、です、ます、etc.)
- Common function words (これ、それ、あれ、etc.)
- Punctuation marks (。、！？、etc.)
- Common pronouns and adverbs

You can modify this file to customize which words are filtered out during indexing.

### Analyzer Pipeline

The Japanese analyzer applies the following processing pipeline:

1. **JapaneseTokenizer**: Kuromoji-based morphological analysis
2. **UpperCaseFilter**: Converts tokens to uppercase
3. **StopFilter**: Removes Japanese stop words

## Dependencies

- Neo4j 2025.06.0 (provided)
- Apache Lucene Kuromoji Analyzer 9.11.1
- Apache Lucene Analysis Common 9.11.1
- JUnit 5.10.1 (test)
- Neo4j Test Harness 5.28.7 (test)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Run the test suite
6. Submit a pull request

## License

This project is licensed under the terms specified in the LICENSE file.

## Troubleshooting

### Analyzer Not Available

If the `japanese` analyzer doesn't appear in the list:

1. Verify the JAR is in the correct `plugins/` directory
2. Check Neo4j logs for any loading errors
3. Ensure Neo4j was restarted after copying the JAR
4. Verify the service file is correctly included in the JAR

### Build Issues

If you encounter build issues:

1. Ensure Java 21+ is installed
2. Verify Maven 3.9+ is available
3. Check internet connectivity for dependency downloads
4. Clear Maven cache: `mvn dependency:purge-local-repository`

### Test Failures

If tests fail:

1. Ensure sufficient memory is available for embedded Neo4j
2. Check for port conflicts (7474, 7687)
3. Verify Japanese text encoding is properly handled (UTF-8)
