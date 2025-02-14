-- Sample data for LINE_ITEM
INSERT INTO LINE_ITEM (ID, VERSION, NAME) VALUES
  ('550e8400-e29b-41d4-a716-446655440000', 1, 'Social Media Ads - Q1'),
  ('550e8400-e29b-41d4-a716-446655440001', 1, 'TV Commercial - Spring'),
  ('550e8400-e29b-41d4-a716-446655440002', 1, 'Print Magazine - Q2'),
  ('550e8400-e29b-41d4-a716-446655440003', 1, 'Google Ads - Summer'),
  ('550e8400-e29b-41d4-a716-446655440004', 1, 'Billboard - Fall'),
  ('550e8400-e29b-41d4-a716-446655440005', 1, 'Influencer Marketing - Q3'),
  ('550e8400-e29b-41d4-a716-446655440006', 1, 'Radio Spot - Winter');

-- Sample data for CATEGORY
INSERT INTO CATEGORY (ID, VERSION, NAME) VALUES
  ('11111111-1111-1111-1111-111111111111', 1, 'Channel'),
  ('22222222-2222-2222-2222-222222222222', 1, 'Region'),
  ('33333333-3333-3333-3333-333333333333', 1, 'Campaign Type');

-- Sample data for CATEGORY_VALUE
INSERT INTO CATEGORY_VALUE (ID, CATEGORY, NAME) VALUES
  ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'Digital'),
  ('aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'TV'),
  ('aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'Print'),
  ('aaaaaaa4-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'Radio'),
  ('aaaaaaa5-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'Outdoor'),
  ('bbbbbbb1-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222', 'North America'),
  ('bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222', 'Europe'),
  ('bbbbbbb3-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222', 'Asia'),
  ('ccccccc1-cccc-cccc-cccc-cccccccccccc', '33333333-3333-3333-3333-333333333333', 'Awareness'),
  ('ccccccc2-cccc-cccc-cccc-cccccccccccc', '33333333-3333-3333-3333-333333333333', 'Conversion'),
  ('ccccccc3-cccc-cccc-cccc-cccccccccccc', '33333333-3333-3333-3333-333333333333', 'Engagement');

-- Sample data for CATEGORIZATION
INSERT INTO CATEGORIZATION (LINE_ITEM, CATEGORY_ID, CATEGORY_VALUE_ID) VALUES
  ('550e8400-e29b-41d4-a716-446655440000', '11111111-1111-1111-1111-111111111111', 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
  ('550e8400-e29b-41d4-a716-446655440000', '22222222-2222-2222-2222-222222222222', 'bbbbbbb1-bbbb-bbbb-bbbb-bbbbbbbbbbbb'),
  ('550e8400-e29b-41d4-a716-446655440000', '33333333-3333-3333-3333-333333333333', 'ccccccc1-cccc-cccc-cccc-cccccccccccc'),
  ('550e8400-e29b-41d4-a716-446655440001', '11111111-1111-1111-1111-111111111111', 'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
  ('550e8400-e29b-41d4-a716-446655440001', '22222222-2222-2222-2222-222222222222', 'bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbbbb'),
  ('550e8400-e29b-41d4-a716-446655440001', '33333333-3333-3333-3333-333333333333', 'ccccccc1-cccc-cccc-cccc-cccccccccccc'),
  ('550e8400-e29b-41d4-a716-446655440002', '11111111-1111-1111-1111-111111111111', 'aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
  ('550e8400-e29b-41d4-a716-446655440002', '22222222-2222-2222-2222-222222222222', 'bbbbbbb1-bbbb-bbbb-bbbb-bbbbbbbbbbbb'),
  ('550e8400-e29b-41d4-a716-446655440002', '33333333-3333-3333-3333-333333333333', 'ccccccc2-cccc-cccc-cccc-cccccccccccc'),
  ('550e8400-e29b-41d4-a716-446655440003', '11111111-1111-1111-1111-111111111111', 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
  ('550e8400-e29b-41d4-a716-446655440003', '22222222-2222-2222-2222-222222222222', 'bbbbbbb3-bbbb-bbbb-bbbb-bbbbbbbbbbbb'),
  ('550e8400-e29b-41d4-a716-446655440003', '33333333-3333-3333-3333-333333333333', 'ccccccc3-cccc-cccc-cccc-cccccccccccc');
