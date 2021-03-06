// 'use strict';

const { NODE_HOST, NODE_PORT } = require('./config');
const express = require('express');
const bodyParser = require('body-parser');
const path = require('path');

// App
const app = express();
app.use('/images', express.static(path.join(__dirname, 'images')));
const userRoutes = require('./routes/user');
const authRoutes = require('./routes/auth');
const wishlistRoutes = require('./routes/wishlist');
const itemRoutes = require('./routes/item');
const bookingRoutes = require('./routes/booking');
const reviewRoutes = require('./routes/review');

app.use(bodyParser.urlencoded({extended: false}));
app.use(bodyParser.json());

// Place your main routers here
app.use('/auth', authRoutes);
app.use('/users', userRoutes);
app.use('/wishlists', wishlistRoutes);
app.use('/items', itemRoutes);
app.use('/bookings', bookingRoutes);
app.use('/reviews', reviewRoutes);
// ... //

app.use((req, res, next) => {
    res.status(404).send("<h1>Welcome to our API</h1>")
});

app.listen(NODE_PORT, NODE_HOST);
console.log(`Running on http://${NODE_HOST}:${NODE_PORT}`);