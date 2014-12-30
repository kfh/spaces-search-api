(ns spaces-search-api.domain.locations
  (:require [schema.core :as s]
            [schema.coerce :as coerce]
            [taoensso.timbre :as timbre]))

(def filters (s/enum :distance-filter :distance-range-filter :polygon-filter :bounding-box-filter))

(def Query {(s/optional-key :lat) double
            (s/optional-key :long) double
            (s/optional-key :lat-1) double
            (s/optional-key :long-1) double
            (s/optional-key :lat-2) double
            (s/optional-key :long-2) double
            (s/optional-key :lat-3) double
            (s/optional-key :long-3) double
            (s/optional-key :lat-top) double
            (s/optional-key :long-top) double
            (s/optional-key :lat-bottom) double
            (s/optional-key :long-bottom) double
            (s/optional-key :distance) String 
            (s/optional-key :from-distance) String
            (s/optional-key :to-distance) String
            (s/required-key :filter) filters})

(def coerce-location-query (coerce/coercer Query coerce/json-coercion-matcher))

(def DistanceFilterQuery
  {(s/required-key :distance) s/Str 
   (s/required-key :lat) double 
   (s/required-key :long) double
   (s/required-key :filter) (s/eq :distance-filter)})

(def DistanceRangeFilterQuery
  {(s/required-key :from-distance) s/Str 
   (s/required-key :to-distance) s/Str 
   (s/required-key :lat) double 
   (s/required-key :long) double
   (s/required-key :filter) (s/eq :distance-range-filter)}) 

(def PolygonFilterQuery
  {(s/required-key :lat-1) double 
   (s/required-key :long-1) double 
   (s/required-key :lat-2) double 
   (s/required-key :long-2) double 
   (s/required-key :lat-3) double 
   (s/required-key :long-3) double
   (s/required-key :filter) (s/eq :polygon-filter)})

(def BoundingBoxFilterQuery
  {(s/required-key :lat-top) double 
   (s/required-key :long-top) double 
   (s/required-key :lat-bottom) double 
   (s/required-key :long-bottom) double
   (s/required-key :filter) (s/eq :bounding-box-filter)})

(def FilterQuery 
  (s/either 
    DistanceFilterQuery
    DistanceRangeFilterQuery
    PolygonFilterQuery
    BoundingBoxFilterQuery))

(defn validate-location-query [query]
  (try
    [(s/validate FilterQuery query) nil]
    (catch clojure.lang.ExceptionInfo e
      [nil (ex-data e)])))

(def Geocodes 
  {(s/required-key :lat) double
   (s/required-key :lon) double})

(def Location
  {(s/required-key :id) s/Str
   (s/required-key :geocodes) Geocodes
   (s/required-key :_timestamp) s/Inst})

(defn validate-location [location]
  (try 
    [(s/validate Location location) nil]
    (catch clojure.lang.ExceptionInfo e
      [nil (ex-data e)])))

(defn validate-location-id [location-id]
  (try 
    [(s/validate s/Str location-id) nil]
    (catch clojure.lang.ExceptionInfo e
      [nil (ex-data e)])))
