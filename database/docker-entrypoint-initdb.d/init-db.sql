-- create table for product

CREATE TABLE product
(
  productid serial UNIQUE PRIMARY KEY,
  description character varying(10485760) NOT NULL,
  image character varying(255) NOT NULL,
  name character varying(255) NOT NULL,
  price double precision NOT NULL
);

ALTER TABLE product
  OWNER TO gordonuser;

ALTER ROLE gordonuser CONNECTION LIMIT -1;

-- add product data
-- note: images are pulled from the public folder at atsea/app/react-app/public
INSERT INTO product (name, description, image, price) VALUES ('Stainless Steel Kitchen Knife Set', 'Professional 8-piece knife set with ergonomic handles and razor-sharp blades', '/images/1.png', 89.99);
INSERT INTO product (name, description, image, price) VALUES ('Non-Stick Ceramic Cookware Set', '12-piece cookware set with ceramic non-stick coating, oven safe up to 500°F', '/images/2.png', 149.99);
INSERT INTO product (name, description, image, price) VALUES ('Cordless Stick Vacuum Cleaner', 'Lightweight cordless vacuum with HEPA filter and 45-minute runtime', '/images/3.png', 199.99);
INSERT INTO product (name, description, image, price) VALUES ('Memory Foam Mattress Topper', '2-inch gel-infused memory foam topper for enhanced comfort and support', '/images/4.png', 79.99);
INSERT INTO product (name, description, image, price) VALUES ('LED Smart Bulbs - 4 Pack', 'Energy-efficient LED bulbs with WiFi connectivity and voice control compatibility', '/images/5.png', 34.99);
INSERT INTO product (name, description, image, price) VALUES ('Bamboo Cutting Board Set', 'Set of 3 bamboo cutting boards in various sizes, naturally antibacterial', '/images/6.png', 29.99);
INSERT INTO product (name, description, image, price) VALUES ('Robot Vacuum with Mapping', 'Smart robot vacuum with laser mapping technology and app control', '/images/7.png', 299.99);
INSERT INTO product (name, description, image, price) VALUES ('Air Fryer - 5.8 Quart', 'Large capacity air fryer with digital controls and multiple cooking functions', '/images/8.png', 129.99);
INSERT INTO product (name, description, image, price) VALUES ('Laundry Basket with Wheels', 'Collapsible laundry basket with wheels and ventilation holes', '/images/9.png', 24.99);
INSERT INTO product (name, description, image, price) VALUES ('Coffee Maker with Grinder', '12-cup programmable coffee maker with built-in burr grinder', '/images/10.png', 89.99);
INSERT INTO product (name, description, image, price) VALUES ('Dish Drying Rack', 'Over-the-sink dish drying rack with draining board and utensil holder', '/images/11.png', 39.99);
INSERT INTO product (name, description, image, price) VALUES ('Blender with Personal Cups', 'High-speed blender with 2 personal blending cups and travel lids', '/images/12.png', 69.99);
INSERT INTO product (name, description, image, price) VALUES ('Shower Curtain with Rings', 'Water-resistant shower curtain with 12 rust-proof metal rings', '/images/13.png', 19.99);
INSERT INTO product (name, description, image, price) VALUES ('Toaster Oven - 6 Slice', 'Convection toaster oven with 6-slice capacity and multiple cooking functions', '/images/14.png', 79.99);
INSERT INTO product (name, description, image, price) VALUES ('Bathroom Scale - Digital', 'Digital bathroom scale with tempered glass platform and backlit display', '/images/15.png', 29.99);
INSERT INTO product (name, description, image, price) VALUES ('Can Opener - Electric', 'Automatic electric can opener with easy-clean design', '/images/16.png', 24.99);
INSERT INTO product (name, description, image, price) VALUES ('Pillow Set - 2 Pack', 'Set of 2 hypoallergenic down alternative pillows with cooling technology', '/images/17.png', 49.99);
INSERT INTO product (name, description, image, price) VALUES ('Waffle Maker - Belgian', 'Belgian waffle maker with non-stick grids and indicator lights', '/images/18.png', 39.99);
INSERT INTO product (name, description, image, price) VALUES ('Laundry Detergent Pods', 'Laundry detergent pods, 81 count, for all machines and water temperatures', '/images/19.png', 14.99);
INSERT INTO product (name, description, image, price) VALUES ('Dish Soap - Lemon Scent', 'Dishwashing liquid with plant-based ingredients and fresh lemon scent', '/images/20.png', 4.99);
INSERT INTO product (name, description, image, price) VALUES ('Paper Towel Holder', 'Stainless steel paper towel holder with tension spring', '/images/21.png', 12.99);
INSERT INTO product (name, description, image, price) VALUES ('Garbage Disposal Cleaner', 'Natural garbage disposal cleaner with baking soda and vinegar', '/images/22.png', 8.99);
INSERT INTO product (name, description, image, price) VALUES ('Oven Mitts - Silicone', 'Heat-resistant silicone oven mitts with comfortable grip', '/images/23.png', 16.99);
INSERT INTO product (name, description, image, price) VALUES ('Bath Towel Set - 6 Piece', '6-piece 100% cotton bath towel set with quick-dry technology', '/images/24.png', 59.99);
INSERT INTO product (name, description, image, price) VALUES ('Refrigerator Magnets - Set of 8', 'Decorative refrigerator magnets with inspirational quotes', '/images/25.png', 9.99);
INSERT INTO product (name, description, image, price) VALUES ('Floor Cleaner - Concentrate', 'Concentrated floor cleaner for hardwood, tile, and laminate floors', '/images/26.png', 12.99);
INSERT INTO product (name, description, image, price) VALUES ('Window Cleaning Kit', 'Complete window cleaning kit with squeegee, bucket, and microfiber cloths', '/images/27.png', 24.99);
INSERT INTO product (name, description, image, price) VALUES ('Bedroom Lamp - Modern', 'Modern LED bedroom lamp with adjustable brightness and USB charging port', '/images/28.png', 45.99);
INSERT INTO product (name, description, image, price) VALUES ('Throw Blanket - Fleece', 'Ultra-soft microfleece throw blanket, machine washable', '/images/29.png', 19.99);
INSERT INTO product (name, description, image, price) VALUES ('Wall Clock - Decorative', 'Decorative wall clock with silent quartz movement and easy-to-read numbers', '/images/30.png', 32.99);
INSERT INTO product (name, description, image, price) VALUES ('Picture Frames - Set of 4', 'Set of 4 assorted size picture frames with easel backs', '/images/31.png', 18.99);
INSERT INTO product (name, description, image, price) VALUES ('Area Rug - 5x7', 'Low-pile area rug with stain-resistant fibers and non-slip backing', '/images/32.png', 89.99);
INSERT INTO product (name, description, image, price) VALUES ('Curtain Panels - Set of 2', 'Light-filtering curtain panels with rod pockets and tiebacks', '/images/33.png', 39.99);
INSERT INTO product (name, description, image, price) VALUES ('Decorative Throw Pillows - Set of 3', 'Set of 3 decorative throw pillows with removable covers', '/images/34.png', 34.99);
INSERT INTO product (name, description, image, price) VALUES ('Wall Art - Canvas Print', 'Gallery-wrapped canvas wall art with fade-resistant inks', '/images/35.png', 69.99);
INSERT INTO product (name, description, image, price) VALUES ('Floor Lamp - Adjustable', 'Adjustable floor lamp with weighted base and energy-efficient bulb', '/images/36.png', 79.99);
INSERT INTO product (name, description, image, price) VALUES ('Coasters - Absorbent Stone', 'Set of 6 absorbent stone coasters, heat-resistant up to 400°F', '/images/37.png', 14.99);
INSERT INTO product (name, description, image, price) VALUES ('Vase - Ceramic', 'Modern ceramic vase with matte finish and wide opening', '/images/38.png', 27.99);
INSERT INTO product (name, description, image, price) VALUES ('Bookends - Metal', 'Heavy-duty metal bookends with non-slip rubber feet', '/images/39.png', 22.99);
INSERT INTO product (name, description, image, price) VALUES ('Wall Shelf - Floating', 'Floating wall shelf with invisible mounting hardware', '/images/40.png', 31.99);
INSERT INTO product (name, description, image, price) VALUES ('Mirror - Wall Mounted', 'Frameless wall-mounted mirror with beveled edges', '/images/41.png', 49.99);
INSERT INTO product (name, description, image, price) VALUES ('Candles - Scented Set', 'Set of 3 soy wax candles with essential oils and cotton wicks', '/images/42.png', 16.99);
INSERT INTO product (name, description, image, price) VALUES ('Room Diffuser - Electric', 'Electric essential oil diffuser with LED lights and timer', '/images/43.png', 29.99);
INSERT INTO product (name, description, image, price) VALUES ('Bathroom Organizer', 'Over-the-toilet bathroom organizer with shelves and hooks', '/images/44.png', 35.99);
INSERT INTO product (name, description, image, price) VALUES ('Shower Caddy - Suction Cup', 'Shower caddy with strong suction cups and rust-resistant coating', '/images/45.png', 12.99);
INSERT INTO product (name, description, image, price) VALUES ('Toothbrush Holder - Wall Mount', 'Wall-mounted toothbrush holder with drip tray', '/images/46.png', 8.99);
INSERT INTO product (name, description, image, price) VALUES ('Soap Dispenser - Automatic', 'Touchless soap dispenser with adjustable portion control', '/images/47.png', 19.99);
INSERT INTO product (name, description, image, price) VALUES ('Bath Mat - Memory Foam', 'Ultra-soft memory foam bath mat with non-slip backing', '/images/48.png', 24.99);
INSERT INTO product (name, description, image, price) VALUES ('Towel Rack - Heated', 'Electric heated towel rack with adjustable temperature', '/images/49.png', 89.99);
INSERT INTO product (name, description, image, price) VALUES ('Step Stool - Folding', 'Folding step stool with wide steps and carrying handle', '/images/50.png', 29.99);
INSERT INTO product (name, description, image, price) VALUES ('Ironing Board Cover', 'Thick ironing board cover with heat-resistant silicone coating', '/images/51.png', 11.99);
INSERT INTO product (name, description, image, price) VALUES ('Lint Roller - Refillable', 'Refillable lint roller with 60 sheets and ergonomic handle', '/images/52.png', 6.99);
INSERT INTO product (name, description, image, price) VALUES ('Clothes Hangers - Velvet', 'Set of 30 velvet-covered clothes hangers with swivel hooks', '/images/53.png', 15.99);
INSERT INTO product (name, description, image, price) VALUES ('Drawer Organizer - 6 Piece', '6-piece drawer organizer set with adjustable compartments', '/images/54.png', 19.99);
INSERT INTO product (name, description, image, price) VALUES ('Shoe Rack - 3 Tier', '3-tier shoe rack with breathable mesh shelves', '/images/55.png', 34.99);
INSERT INTO product (name, description, image, price) VALUES ('Closet Organizer - Hanging', 'Hanging closet organizer with multiple compartments and pockets', '/images/56.png', 22.99);
INSERT INTO product (name, description, image, price) VALUES ('Laundry Hamper - Collapsible', 'Collapsible laundry hamper with reinforced handles', '/images/57.png', 16.99);
INSERT INTO product (name, description, image, price) VALUES ('Iron - Steam', 'Professional steam iron with ceramic soleplate and auto shut-off', '/images/58.png', 49.99);
INSERT INTO product (name, description, image, price) VALUES ('Sewing Kit - Deluxe', 'Deluxe sewing kit with 75 pieces including threads and needles', '/images/59.png', 12.99);
INSERT INTO product (name, description, image, price) VALUES ('First Aid Kit - Home', 'Comprehensive home first aid kit with 200+ pieces', '/images/60.png', 24.99);
INSERT INTO product (name, description, image, price) VALUES ('Digital Thermometer', 'Fast-reading digital thermometer with fever alert', '/images/61.png', 9.99);
INSERT INTO product (name, description, image, price) VALUES ('Blood Pressure Monitor', 'Automatic blood pressure monitor with large display', '/images/62.png', 39.99);
INSERT INTO product (name, description, image, price) VALUES ('Humidifier - Cool Mist', 'Cool mist humidifier with 1-gallon tank and quiet operation', '/images/63.png', 44.99);
INSERT INTO product (name, description, image, price) VALUES ('Air Purifier - HEPA', 'HEPA air purifier with activated carbon filter and air quality sensor', '/images/64.png', 89.99);
INSERT INTO product (name, description, image, price) VALUES ('Fan - Oscillating', '3-speed oscillating desk fan with adjustable height', '/images/65.png', 29.99);
INSERT INTO product (name, description, image, price) VALUES ('Space Heater - Ceramic', 'Ceramic space heater with thermostat and tip-over protection', '/images/66.png', 34.99);
INSERT INTO product (name, description, image, price) VALUES ('Extension Cord - Heavy Duty', 'Heavy-duty extension cord with 3 outlets and 15-foot length', '/images/67.png', 19.99);
INSERT INTO product (name, description, image, price) VALUES ('Power Strip - Surge Protector', '8-outlet power strip with surge protection and USB ports', '/images/68.png', 24.99);
INSERT INTO product (name, description, image, price) VALUES ('Flashlight - LED', 'High-lumen LED flashlight with multiple brightness settings', '/images/69.png', 16.99);
INSERT INTO product (name, description, image, price) VALUES ('Batteries - AA Pack', '24-pack of long-lasting AA alkaline batteries', '/images/70.png', 8.99);
INSERT INTO product (name, description, image, price) VALUES ('Tool Set - 40 Piece', '40-piece household tool set with storage case', '/images/71.png', 39.99);
INSERT INTO product (name, description, image, price) VALUES ('Screwdriver Set - Precision', 'Precision screwdriver set with 24 different tips', '/images/72.png', 14.99);
INSERT INTO product (name, description, image, price) VALUES ('Hammer - Claw', '16 oz claw hammer with fiberglass handle', '/images/73.png', 18.99);
INSERT INTO product (name, description, image, price) VALUES ('Tape Measure - 25 Foot', '25-foot tape measure with lock and belt clip', '/images/74.png', 9.99);
INSERT INTO product (name, description, image, price) VALUES ('Level - Magnetic', '24-inch magnetic level with horizontal and vertical vials', '/images/75.png', 12.99);
INSERT INTO product (name, description, image, price) VALUES ('Paint Brush Set', 'Set of 5 paint brushes in various sizes with synthetic bristles', '/images/76.png', 13.99);
INSERT INTO product (name, description, image, price) VALUES ('Drop Cloth - Canvas', 'Heavy-duty canvas drop cloth, 9x12 feet', '/images/77.png', 21.99);
INSERT INTO product (name, description, image, price) VALUES ('Sandpaper Assortment', 'Assorted grit sandpaper sheets, 50-pack', '/images/78.png', 7.99);
INSERT INTO product (name, description, image, price) VALUES ('Caulk Gun - Professional', 'Professional caulk gun with drip-free design', '/images/79.png', 16.99);
INSERT INTO product (name, description, image, price) VALUES ('Putty Knife Set', 'Set of 4 putty knives in various sizes', '/images/80.png', 11.99);


