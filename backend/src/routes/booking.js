const express = require('express');
const routers = express.Router();
const isAuth = require('../util/auth');
const bookingController = require('../controllers/bookings');
// ALL ROUTES OF WISHLIST
// prefix: /bookings

routers.get('/', isAuth, bookingController.getAllBookingOfUser);
routers.get('/requests', isAuth, bookingController.getAllRequestOfUser);
routers.post('/create', isAuth, bookingController.createBooking);
routers.delete('/delete/:id', isAuth, bookingController.deleteBooking);
routers.put('/accept/:id', isAuth, bookingController.acceptBooking);
routers.put('/done/:id', isAuth, bookingController.doneBooking);
routers.put('/reject/:id', isAuth, bookingController.rejectBooking);

module.exports = routers;