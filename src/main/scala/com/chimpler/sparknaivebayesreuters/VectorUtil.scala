package com.chimpler.sparknaivebayesreuters

import com.google.common.collect.ImmutableBiMap
import scala.collection.JavaConversions._
import org.apache.spark.mllib.linalg.Vectors

class Dictionary(dict: Seq[String]) extends Serializable {

  // map term => index
  val termToIndex = ImmutableBiMap.builder[String, Int]()
    .putAll(dict.zipWithIndex.toMap[String, Int])
    .build()

  @transient
  lazy val indexToTerm = termToIndex.inverse()

  val count = termToIndex.size()

  def indexOf(term: String) = termToIndex(term)

  def valueOf(index: Int) = indexToTerm(index)

  def tfIdfs(terms: Seq[String], idfs: Map[String, Double]) = {
    val filteredTerms = terms.filter(idfs contains)
    (filteredTerms.groupBy(identity).map {
      // mapValues not implemented :-(
      case (term, instances) =>
        (indexOf(term), instances.size / filteredTerms.size * idfs(term))
    }).toSeq.sortBy(_._1) // sort by termId
  }

  def vectorize(tfIdfs: Iterable[(Int, Double)]) = {
    val pairs = tfIdfs.map {
      case (termIndex, tfIdf) => (termIndex, tfIdf)
    }.toSeq

    Vectors.sparse(dict.size, pairs)
  }

  //  def vectorize(words: Seq[String]) = {
  //    // term => number of occurences
  //    val pairs = words.groupBy(identity).mapValues(_.size).map {
  //      case (term, count) => (termToIndex(term), count.toDouble)
  //    }.toSeq
  //    Vectors.sparse(pairs.size, pairs)
  //  }
  //
  //  def toValues(vector: Seq[Int]) = {
  //    vector.map(indexToTerm)
  //  }
}
