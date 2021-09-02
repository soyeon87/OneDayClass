<template>

<v-data-table
    :headers="headers"
    :items="reservationView"
    :items-per-page="5"
    class="elevation-1"
  ></v-data-table>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'ReservationView',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        headers: [
            { text: "id", value: "id" },
            { text: "reservationId", value: "reservationId" },
            { text: "customerId", value: "customerId" },
            { text: "customerName", value: "customerName" },
            { text: "authorId", value: "authorId" },
            { text: "authorName", value: "authorName" },
            { text: "lessonId", value: "lessonId" },
            { text: "lessonName", value: "lessonName" },
            { text: "lessonPrice", value: "lessonPrice" },
            { text: "startDate", value: "startDate" },
            { text: "endDate", value: "endDate" },
            { text: "reservationStatus", value: "reservationStatus" },
            { text: "paymentStatus", value: "paymentStatus" },
        ],
        reservationView : [],
    }),
    async created() {
      var temp = await axios.get(axios.backend + '/reservationviews')

      this.reservationView = temp.data._embedded.reservationviews;

    },
    methods: {
    }
  }
</script>

