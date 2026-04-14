/**
 * BookStore — Sample Data Seeder
 * ================================
 * Run this script AFTER your backend is running to populate sample books
 * and demo user accounts.
 *
 * Usage:
 *   cd docs && npm install && node seed-data.js
 */

const axios = require('axios')
const admin = require('firebase-admin')
const path  = require('path')
const fs    = require('fs')

const BASE = 'http://localhost:8080'
const api  = axios.create({ baseURL: BASE })

// ─── Firebase Admin Init ──────────────────────────────────────────────────────
const SA_PATH = process.env.FIREBASE_SERVICE_ACCOUNT_PATH
  || path.join(__dirname, '../auth-service/firebase-service-account.json')
const serviceAccount = JSON.parse(fs.readFileSync(SA_PATH, 'utf8'))

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  projectId: serviceAccount.project_id,
})
const db = admin.firestore()

// ─── Demo Accounts ────────────────────────────────────────────────────────────
const ACCOUNTS = [
  { name: 'Demo Buyer',  email: 'buyer@demo.com',  password: 'password123', role: 'BUYER'  },
  { name: 'Demo Seller', email: 'seller@demo.com', password: 'password123', role: 'SELLER' },
  { name: 'Admin User',  email: 'admin@demo.com',  password: 'password123', role: 'BUYER'  },
]

// ─── Sample Books ─────────────────────────────────────────────────────────────
const BOOKS = [
  {
    title: 'NCERT Mathematics Class 10',
    author: 'NCERT',
    isbn: '978-8174507037',
    description: 'Complete NCERT Mathematics textbook for Class 10. Covers Real Numbers, Polynomials, Linear Equations, Geometry, Trigonometry, Statistics and Probability.',
    price: 65, originalPrice: 80, stock: 500,
    category: 'Textbook', grade: 'Class 10', subject: 'Mathematics', stream: 'Science',
    publisher: 'NCERT', publishedYear: '2023', language: 'English', pages: 352,
    coverImageUrl: 'https://placehold.co/300x400/2563eb/ffffff?text=NCERT+Math+10',
    featured: true,
  },
  {
    title: 'NCERT Physics Part 1 — Class 12',
    author: 'NCERT',
    isbn: '978-8174507228',
    description: 'NCERT Physics Part 1 for Class 12. Topics include Electric Charges, Current Electricity, Magnetism, and Electromagnetic Induction.',
    price: 75, originalPrice: 90, stock: 350,
    category: 'Textbook', grade: 'Class 12', subject: 'Physics', stream: 'Science',
    publisher: 'NCERT', publishedYear: '2023', language: 'English', pages: 280,
    coverImageUrl: 'https://placehold.co/300x400/7c3aed/ffffff?text=NCERT+Physics+12',
    featured: true,
  },
  {
    title: 'NCERT Chemistry Part 2 — Class 12',
    author: 'NCERT',
    isbn: '978-8174507235',
    description: 'NCERT Chemistry Part 2 for Class 12. Covers Haloalkanes, Aldehydes, Amines, Biomolecules and Polymers.',
    price: 70, originalPrice: 85, stock: 280,
    category: 'Textbook', grade: 'Class 12', subject: 'Chemistry', stream: 'Science',
    publisher: 'NCERT', publishedYear: '2023', language: 'English', pages: 260,
    coverImageUrl: 'https://placehold.co/300x400/059669/ffffff?text=NCERT+Chemistry+12',
    featured: false,
  },
  {
    title: 'RD Sharma Mathematics Class 10',
    author: 'R.D. Sharma',
    isbn: '978-8121900614',
    description: 'Comprehensive Mathematics guide with thousands of solved examples and exercise problems for Class 10 board exams.',
    price: 395, originalPrice: 550, stock: 200,
    category: 'Reference', grade: 'Class 10', subject: 'Mathematics', stream: 'Science',
    publisher: 'Dhanpat Rai', publishedYear: '2023', language: 'English', pages: 1024,
    coverImageUrl: 'https://placehold.co/300x400/dc2626/ffffff?text=RD+Sharma+10',
    featured: true,
  },
  {
    title: 'HC Verma Concepts of Physics Vol 1',
    author: 'H.C. Verma',
    isbn: '978-8177091878',
    description: 'The most loved physics book for JEE preparation. Builds deep conceptual understanding with challenging problems.',
    price: 340, originalPrice: 425, stock: 150,
    category: 'Reference', grade: 'Class 11', subject: 'Physics', stream: 'Science',
    publisher: 'Bharati Bhawan', publishedYear: '2022', language: 'English', pages: 468,
    coverImageUrl: 'https://placehold.co/300x400/1e40af/ffffff?text=HC+Verma+Vol+1',
    featured: true,
  },
  {
    title: 'Accountancy Class 12 — NCERT',
    author: 'NCERT',
    isbn: '978-8174508751',
    description: 'Standard NCERT Accountancy textbook for Class 12 Commerce stream.',
    price: 85, originalPrice: 100, stock: 320,
    category: 'Textbook', grade: 'Class 12', subject: 'Accountancy', stream: 'Commerce',
    publisher: 'NCERT', publishedYear: '2023', language: 'English', pages: 380,
    coverImageUrl: 'https://placehold.co/300x400/d97706/ffffff?text=Accountancy+12',
    featured: false,
  },
  {
    title: 'Business Studies Class 12',
    author: 'NCERT',
    isbn: '978-8174508928',
    description: 'NCERT Business Studies for Class 12. Covers Nature of Business, Business Finance, Marketing and Consumer Protection.',
    price: 75, originalPrice: 90, stock: 290,
    category: 'Textbook', grade: 'Class 12', subject: 'Economics', stream: 'Commerce',
    publisher: 'NCERT', publishedYear: '2023', language: 'English', pages: 302,
    coverImageUrl: 'https://placehold.co/300x400/16a34a/ffffff?text=Business+Studies+12',
    featured: false,
  },
  {
    title: 'History of Modern India',
    author: 'Bipan Chandra',
    isbn: '978-8125025849',
    description: 'Essential reading for UPSC and Class 12 History. Covers Indian Independence Movement in depth.',
    price: 265, originalPrice: 320, stock: 180,
    category: 'Reference', grade: 'Class 12', subject: 'History', stream: 'Arts',
    publisher: 'Orient Blackswan', publishedYear: '2020', language: 'English', pages: 576,
    coverImageUrl: 'https://placehold.co/300x400/be185d/ffffff?text=Modern+India',
    featured: false,
  },
  {
    title: '10 Years Solved Papers — CBSE Class 10',
    author: 'Arihant Experts',
    isbn: '978-9325796065',
    description: 'Last 10 years CBSE board exam solved papers for Class 10. Best revision tool before exams.',
    price: 180, originalPrice: 250, stock: 400,
    category: 'Question Bank', grade: 'Class 10', subject: 'Mathematics', stream: 'Science',
    publisher: 'Arihant', publishedYear: '2024', language: 'English', pages: 320,
    coverImageUrl: 'https://placehold.co/300x400/0891b2/ffffff?text=10+Years+Class+10',
    featured: false,
  },
  {
    title: 'JEE Advanced 2025 — Coming Soon',
    author: 'Multiple Authors',
    isbn: '',
    description: 'Comprehensive JEE Advanced preparation guide for 2025. Will include PYQs from 2014-2024 with detailed solutions.',
    price: 699, originalPrice: 899, stock: 0,
    category: 'Guide', grade: 'Class 12', subject: 'Mathematics', stream: 'Science',
    publisher: 'Disha Publication', publishedYear: '2025', language: 'English', pages: 1200,
    coverImageUrl: 'https://placehold.co/300x400/6366f1/ffffff?text=JEE+Advanced+2025',
    comingSoon: true,
    featured: false,
  },
  {
    title: 'English Literature — Flamingo Class 12',
    author: 'NCERT',
    isbn: '978-8174508782',
    description: 'NCERT Flamingo English textbook for Class 12. Includes prose and poetry by Indian and world authors.',
    price: 60, stock: 600,
    category: 'Textbook', grade: 'Class 12', subject: 'English', stream: 'Arts',
    publisher: 'NCERT', publishedYear: '2023', language: 'English', pages: 180,
    coverImageUrl: 'https://placehold.co/300x400/0f766e/ffffff?text=Flamingo+12',
    featured: false,
  },
  {
    title: 'Computer Science with Python — Class 11',
    author: 'Sumita Arora',
    isbn: '978-8176489539',
    description: 'Most popular Computer Science textbook for Class 11. Covers Python basics, data structures and databases with practical exercises.',
    price: 450, originalPrice: 580, stock: 120,
    category: 'Textbook', grade: 'Class 11', subject: 'Computer Science', stream: 'Science',
    publisher: 'Dhanpat Rai', publishedYear: '2023', language: 'English', pages: 650,
    coverImageUrl: 'https://placehold.co/300x400/1d4ed8/ffffff?text=CS+Python+11',
    featured: true,
  },
]

// ─── Helpers ─────────────────────────────────────────────────────────────────
async function findUserByEmail(email) {
  const snap = await db.collection('users').where('email', '==', email).limit(1).get()
  if (snap.empty) return null
  return { id: snap.docs[0].id, ...snap.docs[0].data() }
}

// ─── Main Seeder ──────────────────────────────────────────────────────────────
async function seed() {
  console.log('\n===== BookStore Data Seeder =====\n')

  // 1. Register demo accounts
  console.log('1. Creating demo accounts...')
  for (const account of ACCOUNTS) {
    try {
      await api.post('/api/auth/register', account)
      console.log(`   ✓ Registered: ${account.email} (${account.role})`)
    } catch (e) {
      const msg = e.response?.data?.message || e.message
      if (msg && msg.toLowerCase().includes('already')) {
        console.log(`   ~ Already exists: ${account.email}`)
      } else {
        console.error(`   ✗ ${account.email}:`, msg)
      }
    }
  }

  // 2. Promote admin@demo.com to ADMIN role via Firestore directly
  console.log('\n2. Promoting admin@demo.com to ADMIN role...')
  const adminUser = await findUserByEmail('admin@demo.com')
  if (adminUser) {
    await db.collection('users').doc(adminUser.id).update({ role: 'ADMIN', status: 'ACTIVE' })
    console.log(`   ✓ admin@demo.com promoted to ADMIN (id: ${adminUser.id})`)
  } else {
    console.error('   ✗ admin@demo.com not found in Firestore')
  }

  // 3. Approve seller@demo.com directly in Firestore
  console.log('\n3. Activating seller@demo.com...')
  const sellerUser = await findUserByEmail('seller@demo.com')
  if (sellerUser) {
    await db.collection('users').doc(sellerUser.id).update({ status: 'ACTIVE' })
    console.log(`   ✓ seller@demo.com activated (id: ${sellerUser.id})`)
  } else {
    console.error('   ✗ seller@demo.com not found in Firestore')
  }

  // 4. Login as seller to get token
  console.log('\n4. Logging in as seller...')
  let sellerToken = null
  try {
    const { data } = await api.post('/api/auth/login', { email: 'seller@demo.com', password: 'password123' })
    sellerToken = data.accessToken
    console.log('   ✓ Seller login successful')
  } catch (e) {
    console.error('   ✗ Seller login failed:', e.response?.data?.message || e.message)
  }

  // 5. Seed books
  console.log('\n5. Seeding books...')
  if (!sellerToken) {
    console.error('   ✗ No seller token — skipping books.')
  } else {
    let seeded = 0
    for (const book of BOOKS) {
      try {
        await api.post('/api/books', book, {
          headers: { Authorization: `Bearer ${sellerToken}` }
        })
        seeded++
        console.log(`   ✓ "${book.title}"`)
      } catch (e) {
        console.error(`   ✗ "${book.title}":`, e.response?.data?.message || e.message)
      }
    }
    console.log(`\n   Books seeded: ${seeded}/${BOOKS.length}`)
  }

  console.log('\n===== Seeding Complete =====')
  console.log('\nDemo Login Credentials:')
  console.log('   BUYER  : buyer@demo.com  / password123')
  console.log('   SELLER : seller@demo.com / password123')
  console.log('   ADMIN  : admin@demo.com  / password123')
  console.log('\nOpen http://localhost:3001 to use the app.\n')
  process.exit(0)
}

seed().catch(e => {
  console.error('Seeder error:', e.message)
  process.exit(1)
})
