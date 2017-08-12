(ns ez-report.deps
  (:require [ez-report.util :as util]))

;deps
(def jpeg-camera {:js ["/public/jpeg_camera.min.js"]})
(def opentok {:js ["//static.opentok.com/webrtc/v2.10.2/js/TB.min.js"]})
(def fullcalendar {:css ["/public/fullcalendar-3.2.0/fullcalendar.min.css"]
                   :js ["/public/bower_compact/moment.min.js"
                        "/public/fullcalendar-3.2.0/fullcalendar.min.js"]})
(def jquery {:js ["/public/jquery.min.js"]})
(def bootstrap {:css ["/public/bootstrap.min.css"]
                :js (concat (:js jquery) ["/public/bootstrap.min.js"])})
(def moment {:js ["/public/bower_compact/moment.min.js"]})
(def moment-timezone {:js ["/public/bower_compact/moment.min.js"
                           "/public/bower_compact/moment-timezone-with-data-2010-2020.js"]})
(def datetimepicker {:css (concat
                            ["/public/bower_compact/bootstrap-datetimepicker.min.css"])
                     :js (concat
                           (moment :js)
                           ;(bootstrap :js)
                           ["/public/bower_compact/bootstrap-datetimepicker.min.js"])})
#_(def jquery-ui {:css ["/public/jquery-ui.min.css"]
                :js ["/public/jquery-ui.min.js"]})
(def bootstrap-select {:css (concat
                              ;(bootstrap :css)
                              ["/public/css/bootstrap-select.min.css"])
                       :js (concat
                             ;(bootstrap :js)
                             ["/public/bootstrap-select.min.js"])})
(def bootstrap4-select {:css ["/public/bootstrap4-select.min.css"]
                        :js ["/public/bootstrap4-select.min.js"]})
(def draft {:css ["/public/css/mail.css"
                  (if util/dev?
                    "/public/css/Draft.min.css"
                    "https://cdnjs.cloudflare.com/ajax/libs/draft-js/0.10.0/Draft.min.css")]})
(def stripe {:js ["https://js.stripe.com/v2/"]})
(def stripe-checkout {:js ["https://checkout.stripe.com/checkout.js"]})
#_(def rating {:js ["/public/rating.min.js"]
               :css ["/public/css/rating.min.css"]})
(def rc-rate {:css ["/public/css/rate.css"]})
(def saas {:css ["public/saas-assets/css/core.min.css"
                 "public/saas-assets/css/thesaas.min.css"
                 "public/saas-assets/css/style.css"]
           :js ["public/saas-assets/js/core.min.js"
                "public/saas-assets/js/thesaas.min.js"
                "public/saas-assets/js/script.js"]})
(def bootstrap4 {:css ["/public/bootstrap4.min.css"]
                 :js (concat (:js saas) ["/public/bootstrap4.min.js"])})
(def selectize {:css ["/public/selectize/stylesheet.css"
                      "/public/selectize/selectize.bootstrap3.css"]
                :js ["/public/selectize/selectize.min.js"]})
