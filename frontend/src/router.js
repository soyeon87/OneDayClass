
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import ReservationManager from "./components/ReservationManager"

import PaymentManager from "./components/PaymentManager"

import LessonManager from "./components/LessonManager"


import ReservationView from "./components/ReservationView"
export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/reservations',
                name: 'ReservationManager',
                component: ReservationManager
            },

            {
                path: '/payments',
                name: 'PaymentManager',
                component: PaymentManager
            },

            {
                path: '/lessons',
                name: 'LessonManager',
                component: LessonManager
            },


            {
                path: '/reservationViews',
                name: 'ReservationView',
                component: ReservationView
            },


    ]
})
