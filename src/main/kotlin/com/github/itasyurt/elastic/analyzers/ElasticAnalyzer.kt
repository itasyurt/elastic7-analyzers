package com.github.itasyurt.elastic.analyzers

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.CharArraySet
import org.apache.lucene.analysis.StopFilter
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter
import org.apache.lucene.analysis.core.LowerCaseFilter
import org.apache.lucene.analysis.core.WhitespaceTokenizer
import org.apache.lucene.analysis.synonym.SolrSynonymParser
import org.apache.lucene.analysis.synonym.SynonymGraphFilter
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.elasticsearch.index.analysis.CharFilterFactory
import org.elasticsearch.index.analysis.CustomAnalyzer
import org.elasticsearch.index.analysis.TokenFilterFactory
import org.elasticsearch.index.analysis.TokenizerFactory
import java.io.Reader
import java.util.function.Supplier

class ElasticAnalyzer {


    fun analyze(text: String): List<String> {
        val result = mutableListOf<String>()

        val analyzer = createAnalyzer()
        var ts = analyzer.tokenStream("", text)
        ts.reset()
        while (ts.incrementToken()) {
            val attr = ts.getAttribute(CharTermAttribute::class.java)
            result.add(attr.toString())
        }
        return result


    }

    private fun createAnalyzer(): Analyzer {
        val tokenizerFactory = createTokenizerFactory()
        val charFilterFactories = createCharFilterFactories()
        val tokenFilterFactories = createTokenFilterFactories()

        return CustomAnalyzer(tokenizerFactory, charFilterFactories, tokenFilterFactories)
    }

    private fun createTokenFilterFactories(): Array<TokenFilterFactory> {
        val stopwords = CharArraySet(listOf("a", "an", "on"), false)
        val stopTokenFilterFactory = object : TokenFilterFactory {
            override fun create(tokenStream: TokenStream?) = StopFilter(tokenStream, stopwords)
            override fun name() = "stop"
        }


        val lowercaseFilterFactory = object : TokenFilterFactory {
            override fun create(tokenStream: TokenStream?) = LowerCaseFilter(tokenStream)

            override fun name() = "lower"

        }


        val synonymAnalyzer = object : Analyzer() {
            override fun createComponents(fieldName: String?): TokenStreamComponents {
                val tokenizer = WhitespaceTokenizer()
                val stream = LowerCaseFilter(tokenizer)
                return TokenStreamComponents(tokenizer, stream)
            }

        }
        val synonymParser = SolrSynonymParser(true, false, synonymAnalyzer)

        val synoymsList = listOf("blog post=>blogpost")
        synonymParser.parse(synoymsList.joinToString("\n").reader())


        val synonymTokenFilterFactory = object : TokenFilterFactory {
            override fun create(tokenStream: TokenStream?) =
                SynonymGraphFilter(tokenStream, synonymParser.build(), false)

            override fun name() = "synonyms"

        }
        return arrayOf(lowercaseFilterFactory, synonymTokenFilterFactory, stopTokenFilterFactory)
    }

    private fun createCharFilterFactories(): Array<CharFilterFactory> {
        val htmlStripFilterFactory = object : CharFilterFactory {
            override fun name() = "htmlStrip"

            override fun create(reader: Reader?) = HTMLStripCharFilter(reader)


        }
        val charFilterFactories = arrayOf<CharFilterFactory>(htmlStripFilterFactory)
        return charFilterFactories
    }

    private fun createTokenizerFactory(): TokenizerFactory {
        return TokenizerFactory.newFactory("whitespace",
            Supplier { WhitespaceTokenizer() }
        )
    }


}