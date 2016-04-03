(ns spaces-search-api.domain.locations
  (:require [schema.core :as s]))

(def filters (s/enum :distance-filter :distance-range-filter :polygon-filter :bounding-box-filter))

(s/defschema DistanceFilterQuery
  {:distance s/Str
   :lat Double
   :long Double
   :filter (s/eq :distance-filter)})

(s/defschema DistanceRangeFilterQuery
  {:from-distance s/Str
   :to-distance s/Str
   :lat Double
   :long Double
   :filter (s/eq :distance-range-filter)})

(s/defschema PolygonFilterQuery
  {:lat-1 Double
   :long-1 Double
   :lat-2 Double
   :long-2 Double
   :lat-3 Double
   :long-3 Double
   :filter (s/eq :polygon-filter)})

(s/defschema BoundingBoxFilterQuery
  {:lat-top Double
   :long-top Double
   :lat-bottom Double
   :long-bottom Double
   :filter (s/eq :bounding-box-filter)})

(s/defschema FilterQuery
  (s/either
    DistanceFilterQuery
    DistanceRangeFilterQuery
    PolygonFilterQuery
    BoundingBoxFilterQuery))

(s/defschema Geocodes
  {:lat Double
   :lon Double})

(s/defschema Location
  {:id s/Str
   :geocodes Geocodes
   :timestamp s/Inst
   (s/optional-key :refresh) s/Bool})

(s/defschema NewLocation (dissoc Location :location/public-id))


