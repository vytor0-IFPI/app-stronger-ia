-- ==============================================================================
-- STRONGER CAST - Supabase Database Schema & Row Level Security (RLS)
-- 100% Free Tier Compatible | PostgreSQL / Supabase
-- ==============================================================================

-- 1. Profiles Table (Linked with Supabase Auth users)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT,
    nome TEXT,
    admin BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Perfis visíveis para o próprio usuário e admins"
ON public.profiles FOR SELECT
TO authenticated, anon
USING (auth.uid() = id OR (SELECT admin FROM public.profiles WHERE id = auth.uid()) = TRUE);

CREATE POLICY "Usuários podem atualizar seus próprios perfis"
ON public.profiles FOR UPDATE
TO authenticated
USING (auth.uid() = id);

-- 2. Trigger para criar perfil automaticamente no cadastro (Auth Hook)
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.profiles (id, email, nome, admin)
  VALUES (
    NEW.id,
    NEW.email,
    COALESCE(NEW.raw_user_meta_data->>'nome', split_part(NEW.email, '@', 1)),
    -- Primeiro usuário ou email admin recebe permissão de administrador
    CASE WHEN NEW.email ILIKE '%admin%' THEN TRUE ELSE FALSE END
  );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- 3. Tabela de Episódios
CREATE TABLE IF NOT EXISTS public.episodes (
    id BIGSERIAL PRIMARY KEY,
    titulo TEXT NOT NULL,
    convidado TEXT,
    data TEXT NOT NULL,
    youtube TEXT,
    spotify TEXT,
    mp3 TEXT,
    capa TEXT,
    publicado BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

ALTER TABLE public.episodes ENABLE ROW LEVEL SECURITY;

-- Leitura pública para itens publicados (ou todos para admin)
CREATE POLICY "Episódios publicados visíveis publicamente"
ON public.episodes FOR SELECT
TO anon, authenticated
USING (publicado = TRUE OR (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND admin = TRUE)));

-- Escrita restrita a administradores
CREATE POLICY "Admins podem criar episódios"
ON public.episodes FOR INSERT
TO authenticated
WITH CHECK (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND admin = TRUE));

CREATE POLICY "Admins podem atualizar episódios"
ON public.episodes FOR UPDATE
TO authenticated
USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND admin = TRUE));

CREATE POLICY "Admins podem excluir episódios"
ON public.episodes FOR DELETE
TO authenticated
USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND admin = TRUE));

-- 4. Tabela de Devocionais
CREATE TABLE IF NOT EXISTS public.devotionals (
    id BIGSERIAL PRIMARY KEY,
    titulo TEXT NOT NULL,
    versiculo TEXT NOT NULL,
    texto TEXT NOT NULL,
    data TEXT NOT NULL,
    publicado BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

ALTER TABLE public.devotionals ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Devocionais publicados visíveis publicamente"
ON public.devotionals FOR SELECT
TO anon, authenticated
USING (publicado = TRUE OR (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND admin = TRUE)));

CREATE POLICY "Admins podem criar devocionais"
ON public.devotionals FOR INSERT
TO authenticated
WITH CHECK (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND admin = TRUE));

CREATE POLICY "Admins podem editar devocionais"
ON public.devotionals FOR UPDATE
TO authenticated
USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND admin = TRUE));

CREATE POLICY "Admins podem excluir devocionais"
ON public.devotionals FOR DELETE
TO authenticated
USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND admin = TRUE));

-- 5. Tabela de Canais Recomendados
CREATE TABLE IF NOT EXISTS public.recommended_channels (
    id BIGSERIAL PRIMARY KEY,
    titulo TEXT NOT NULL,
    canal TEXT NOT NULL,
    link TEXT NOT NULL,
    tipo TEXT NOT NULL CHECK (tipo IN ('video', 'canal')),
    publicado BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

ALTER TABLE public.recommended_channels ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Canais publicados visíveis publicamente"
ON public.recommended_channels FOR SELECT
TO anon, authenticated
USING (publicado = TRUE OR (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND admin = TRUE)));

CREATE POLICY "Admins podem criar canais"
ON public.recommended_channels FOR INSERT
TO authenticated
WITH CHECK (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND admin = TRUE));

CREATE POLICY "Admins podem editar canais"
ON public.recommended_channels FOR UPDATE
TO authenticated
USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND admin = TRUE));

CREATE POLICY "Admins podem excluir canais"
ON public.recommended_channels FOR DELETE
TO authenticated
USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND admin = TRUE));

-- 6. Storage Bucket para uploads de Mídias (MP3 e Capas até 50MB)
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
    'media',
    'media',
    TRUE,
    52428800, -- 50MB limit
    ARRAY['audio/mpeg', 'audio/mp3', 'image/jpeg', 'image/png', 'image/webp']
)
ON CONFLICT (id) DO NOTHING;

CREATE POLICY "Mídias públicas para leitura"
ON storage.objects FOR SELECT
TO anon, authenticated
USING (bucket_id = 'media');

CREATE POLICY "Apenas admins podem fazer upload de mídia"
ON storage.objects FOR INSERT
TO authenticated
WITH CHECK (bucket_id = 'media' AND (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND admin = TRUE)));

CREATE POLICY "Apenas admins podem atualizar mídia"
ON storage.objects FOR UPDATE
TO authenticated
USING (bucket_id = 'media' AND (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND admin = TRUE)));

CREATE POLICY "Apenas admins podem apagar mídia"
ON storage.objects FOR DELETE
TO authenticated
USING (bucket_id = 'media' AND (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND admin = TRUE)));

-- 7. Dados Iniciais de Demonstração (Seed Data)
INSERT INTO public.episodes (titulo, convidado, data, youtube, spotify, mp3, capa, publicado)
VALUES 
(
    'Identidade Inabalável na Juventude',
    'Pr. Lucas Santana',
    '24/10/2026',
    'https://www.youtube.com/watch?v=dQw4w9WgXcQ',
    'https://open.spotify.com/show/strongercast',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3',
    'https://images.unsplash.com/photo-1519791883288-dc8bd696e667?w=800',
    TRUE
),
(
    'Vencendo a Ansiedade com Fé Real',
    'Dra. Gabriela Nunes (Psicóloga Cristã)',
    '17/10/2026',
    'https://www.youtube.com/watch?v=ScMzIvxBSi4',
    'https://open.spotify.com/show/strongercast',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
    'https://images.unsplash.com/photo-1499209974431-9dddcece7f88?w=800',
    TRUE
),
(
    'Propósito & Carreira: Onde Deus Te Quer?',
    'Felipe Rocha',
    '10/10/2026',
    'https://www.youtube.com/watch?v=9bZkp7q19f0',
    'https://open.spotify.com/show/strongercast',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3',
    'https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=800',
    TRUE
)
ON CONFLICT DO NOTHING;

INSERT INTO public.devotionals (titulo, versiculo, texto, data, publicado)
VALUES
(
    'A Certeza daquilo que não vemos',
    'Hebreus 11.1',
    'Ora, a fé é a certeza daquilo que esperamos e a convicção do que não vemos. Quando o mundo cobra provas imediatas e palpáveis, a fé nos ancora na fidelidade eterna de Deus. Não baseie sua caminhada pelo que seus olhos naturais observam hoje; firme seus passos na promessa Daquele que é fiel para cumprir.',
    '25/10/2026',
    TRUE
),
(
    'Coragem no Meio da Tempestade',
    'Josué 1.9',
    'Não fui eu que lhe ordenei? Seja forte e corajoso! Não se apavore nem desanime, pois o Senhor, o seu Deus, estará com você por onde você andar. A coragem não é ausência de medo, mas a certeza inegociável de que Deus é maior que qualquer desafio na sua universidade, trabalho ou família.',
    '23/10/2026',
    TRUE
),
(
    'Renovando a Mente Diariamente',
    'Romanos 12.2',
    'E não vos conformeis com este século, mas transformai-vos pela renovação da vossa mente, para que experimenteis qual seja a boa, agradável e perfeita vontade de Deus. Filtre o que entra pelas suas telas e ouvidos. Alimente seu espírito com a Verdade.',
    '20/10/2026',
    TRUE
)
ON CONFLICT DO NOTHING;

INSERT INTO public.recommended_channels (titulo, canal, link, tipo, publicado)
VALUES
(
    'Como Ouvir a Voz de Deus',
    'Deive Leonardo',
    'https://www.youtube.com/watch?v=aircAruvnKk',
    'video',
    TRUE
),
(
    'Canal Oficial Dunamis Movement',
    'Dunamis Movement',
    'https://www.youtube.com/@DunamisMovement',
    'canal',
    TRUE
),
(
    'Lidando com Pressão e Expectativas',
    'Teo Hayashi',
    'https://www.youtube.com/watch?v=kJQP7kiw5Fk',
    'video',
    TRUE
),
(
    'Canal JesusCopy',
    'Douglas Gonçalves',
    'https://www.youtube.com/@Jesuscopy',
    'canal',
    TRUE
)
ON CONFLICT DO NOTHING;
