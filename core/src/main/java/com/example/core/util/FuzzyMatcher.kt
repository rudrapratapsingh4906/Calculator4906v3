package com.example.core.util

import kotlin.math.max
import kotlin.math.min

object FuzzyMatcher {
    fun calculateLevenshteinSimilarity(s1: String, s2: String): Double {
        val str1 = s1.lowercase().trim()
        val str2 = s2.lowercase().trim()
        if (str1 == str2) return 1.0
        if (str1.isEmpty() || str2.isEmpty()) return 0.0

        val dp = IntArray(str2.length + 1) { it }
        for (i in 1..str1.length) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..str2.length) {
                val temp = dp[j]
                if (str1[i - 1] == str2[j - 1]) {
                    dp[j] = prev
                } else {
                    dp[j] = min(prev, min(dp[j - 1], dp[j])) + 1
                }
                prev = temp
            }
        }
        val maxLen = max(str1.length, str2.length)
        return (maxLen - dp[str2.length]).toDouble() / maxLen
    }

    fun isMatch(spoken: String, aliases: List<String>, threshold: Double = 0.75): Boolean {
        val cleanSpoken = spoken.lowercase().trim()
        for (alias in aliases) {
            val cleanAlias = alias.lowercase().trim()
            if (cleanAlias.isEmpty()) continue
            
            // Exact match or substring contains
            if (cleanSpoken == cleanAlias || cleanSpoken.contains(cleanAlias) || cleanAlias.contains(cleanSpoken)) {
                return true
            }

            // Substring word match (extremely good for voice prompts with filler words)
            val spokenWords = cleanSpoken.split(Regex("\\s+")).toSet()
            val aliasWords = cleanAlias.split(Regex("\\s+")).toSet()
            if (aliasWords.isNotEmpty() && spokenWords.containsAll(aliasWords)) {
                return true
            }

            // Fuzzy similarity
            val sim = calculateLevenshteinSimilarity(cleanSpoken, cleanAlias)
            if (sim >= threshold) {
                return true
            }

            // Check if any sub-phrase of spoken matches the alias fuzzily
            val words = cleanSpoken.split(Regex("\\s+"))
            val aliasWordCount = cleanAlias.split(Regex("\\s+")).size
            if (words.size > aliasWordCount && aliasWordCount > 0) {
                for (start in 0..words.size - aliasWordCount) {
                    val subPhrase = words.subList(start, start + aliasWordCount).joinToString(" ")
                    if (calculateLevenshteinSimilarity(subPhrase, cleanAlias) >= threshold) {
                        return true
                    }
                }
            }
        }
        return false
    }
}
