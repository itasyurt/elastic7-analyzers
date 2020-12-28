package com.github.itasyurt.elastic.analyzers
import org.junit.Assert
import org.junit.Test

class AnalyzerTest
{
    @Test
    fun testTokensAnalyzed() {

        val analyzer = ElasticAnalyzer()

        val tokens = analyzer.analyze("A Blog Post on <b>Elasticsearch</b>")

        Assert.assertEquals(listOf("blogpost", "elasticsearch"), tokens)

    }
}