-- USERS
INSERT INTO users (id, username, password_hash, email, role) VALUES
('b4a3f3d0-dad5-4c97-a022-3e4ac18498b4', 'sophie.miller', '$2b$12$Dc8X0UZTY6GV7A8nsGQzJumyy0LZr/f3YInCv6PbPJo2t1m7JeH3i', 'sophie.miller@example.com', 'GENERAL'),
('e0284737-9f11-4f80-a732-8fae9fcd7ebd', 'vintage.dan', '$2b$12$V1FgU0a6qChS/E5o6N3eZekjUFeUIZ9F3VvRxUgIh88Hc53V3ENPu', 'dan.vintage@example.com', 'ADMIN'),
('87c8c198-3a1f-4264-9014-42caa723b313', 'marina_kolov', '$2b$12$1ml2v0ljd54Z7WZxsqW0xe0fVOsMQLS6wNvHTDtxjzOaEfRogvy6G', 'marina.kolov@example.org', 'GENERAL'),
('cabfab2b-18cb-4f92-bd4a-b586c7985eac', 'collector.lee', '$2b$12$7O2U19CzRL9jPQZKwjE8HeuI.4A2xJ7g/xeZf6ULzDuMtfzWmDvZK', 'lee.c@example.net', 'ADMIN'),
('56d7d61e-0d4c-4d39-8860-6f46f435f2ea', 'ana_gomez', '$2b$12$3R0zYozuqZGxWkwJ2KR0neZKhUkU0dfM9OD0KTu9xquUs9Y92XZra', 'ana.g@example.com', 'GENERAL');

-- ITEMS
INSERT INTO items (id, name, type, description, production_year, country, condition, image_path) VALUES
('27430c82-74fc-44a2-a354-c2692c2e7fa3', 'Napoleon III Coin', 'COIN', 'French 10 Francs gold coin minted in 1862.', '1862', 'France', 'GOOD', '/images/dog.png'),
('98138a54-0a3d-42b0-bc80-4b893e1a36ef', 'Ming Dynasty Vase', 'ANTIQUE', 'Porcelain vase from early Ming dynasty.', '1420', 'China', 'EXCELLENT', '/images/dog.png'),
('8b6c593f-3e68-44b0-8c3b-7a8e3d15c874', 'Ancient Greek Tetradrachm', 'COIN', 'Silver coin featuring Alexander the Great.', '320', 'Greece', 'FAIR', '/images/dog.png'),
('1e6a980f-cc0e-4ba5-bc53-37b1ffb1425b', 'Art Nouveau Clock', 'ANTIQUE', 'Handcrafted brass clock from 1905.', '1905', 'Belgium', 'GOOD', '/images/dog.png'),
('f340bf09-e054-4f6f-bb0a-77022c15d88a', 'Byzantine Cross Pendant', 'ANTIQUE', 'Bronze cross from Byzantine Empire.', 'unknown', 'Turkey', 'POOR', '/images/dog.png');

-- COLLECTIONS
INSERT INTO collections (id, user_id, name, created_at) VALUES
('1a10c3a9-62c0-4be6-a1f1-79299f473bdf', 'b4a3f3d0-dad5-4c97-a022-3e4ac18498b4', 'Imperial Coins', '2024-09-12 15:24:00'),
('74c1a38d-dad1-4e4d-9a90-d7c84cd0fafd', 'e0284737-9f11-4f80-a732-8fae9fcd7ebd', 'Dynastic Relics', '2024-12-02 09:41:30'),
('6674705e-89a5-46b3-8dc3-4a3589b7dc83', '87c8c198-3a1f-4264-9014-42caa723b313', 'Greek Antiquities', '2025-01-10 11:12:45'),
('f4d5ad36-c02f-41ed-b47f-861b47a17b2c', 'cabfab2b-18cb-4f92-bd4a-b586c7985eac', 'Timeless Pieces', '2025-02-20 17:55:00'),
('39c2efc4-5196-4cf0-b476-4d9179c7cc53', '56d7d61e-0d4c-4d39-8860-6f46f435f2ea', 'Sacred Artifacts', '2025-03-05 13:17:15');

-- TRANSACTIONS
INSERT INTO transactions (id, user_id, item_id, type, timestamp) VALUES
('3f99a645-3b1c-4ad3-9f4e-6b8c71f2dc8c', 'b4a3f3d0-dad5-4c97-a022-3e4ac18498b4', '27430c82-74fc-44a2-a354-c2692c2e7fa3', 'PURCHASE', '2024-09-15 10:45:00'),
('40b78172-b064-408b-9731-b0ff3c8cf131', 'e0284737-9f11-4f80-a732-8fae9fcd7ebd', '98138a54-0a3d-42b0-bc80-4b893e1a36ef', 'PURCHASE', '2024-12-10 08:21:30'),
('6d45fa15-80be-4a49-9a17-9f7bda3e8e5e', '87c8c198-3a1f-4264-9014-42caa723b313', '8b6c593f-3e68-44b0-8c3b-7a8e3d15c874', 'SALE', '2025-01-15 14:02:00'),
('b1b7550c-90df-4cfc-a2fc-5cd3b874a12a', 'cabfab2b-18cb-4f92-bd4a-b586c7985eac', '1e6a980f-cc0e-4ba5-bc53-37b1ffb1425b', 'PURCHASE', '2025-02-25 16:10:10'),
('a8f20b49-9b1a-44b4-9d9f-1c2d94d1c82d', '56d7d61e-0d4c-4d39-8860-6f46f435f2ea', 'f340bf09-e054-4f6f-bb0a-77022c15d88a', 'PURCHASE', '2025-03-09 12:00:00');

-- item_collection
INSERT INTO item_collection (collection_id, item_id) VALUES
('1a10c3a9-62c0-4be6-a1f1-79299f473bdf', '27430c82-74fc-44a2-a354-c2692c2e7fa3'),
('74c1a38d-dad1-4e4d-9a90-d7c84cd0fafd', '98138a54-0a3d-42b0-bc80-4b893e1a36ef'),
('6674705e-89a5-46b3-8dc3-4a3589b7dc83', '8b6c593f-3e68-44b0-8c3b-7a8e3d15c874'),
('f4d5ad36-c02f-41ed-b47f-861b47a17b2c', '1e6a980f-cc0e-4ba5-bc53-37b1ffb1425b'),
('39c2efc4-5196-4cf0-b476-4d9179c7cc53', 'f340bf09-e054-4f6f-bb0a-77022c15d88a'),
('39c2efc4-5196-4cf0-b476-4d9179c7cc53', '27430c82-74fc-44a2-a354-c2692c2e7fa3');
