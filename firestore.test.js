const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_customer_1";
const BOB_UID = "bob_customer_2";
const OWNER_UID = "owner_msr_admin";
const OWNER_EMAIL = "mdseamrahman2525@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastDate(msOffset = 5000) {
  return new Date(Date.now() - msOffset);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: denied across all collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("games").where("active", "==", true).get());
  await assertFails(unauthDb.collection("products").where("active", "==", true).get());
  await assertFails(unauthDb.collection("orders").where("userId", "==", ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("app_settings").doc("store_config").get());
});

test("Email spoofing defense: unverified owner email cannot perform admin actions", async () => {
  const spoofDb = testEnv.authenticatedContext("spoof_uid", {
    email: OWNER_EMAIL,
    email_verified: false,
  }).firestore();

  const now = pastDate();
  await assertFails(
    spoofDb.collection("games").doc("roblox").set({
      name: "Roblox",
      slug: "roblox",
      description: "Official Roblox top-up",
      imageUrl: "",
      themeColorHex: "#1E88E5",
      active: true,
      featured: true,
      displayOrder: 1,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Owner admin: can manage games, categories, products, store config, bonus offers, FAQs, and announcements", async () => {
  const ownerDb = testEnv.authenticatedContext(OWNER_UID, {
    email: OWNER_EMAIL,
    email_verified: true,
  }).firestore();
  const now = pastDate();

  await assertSucceeds(
    ownerDb.collection("games").doc("roblox").set({
      name: "Roblox",
      slug: "roblox",
      description: "Instant Robux and Roblox Plus subscriptions",
      imageUrl: "",
      themeColorHex: "#1E88E5",
      active: true,
      featured: true,
      displayOrder: 1,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    ownerDb.collection("categories").doc("roblox_robux").set({
      gameId: "roblox",
      name: "Roblox Robux",
      description: "Official Robux packages",
      type: "CURRENCY",
      displayOrder: 1,
      active: true,
      requiredInputFields: ["Roblox Username"],
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    ownerDb.collection("products").doc("robux_25").set({
      gameId: "roblox",
      categoryId: "roblox_robux",
      categoryType: "CURRENCY",
      name: "25 Robux",
      subtitle: "Instant Digital Top-Up",
      description: "Official 25 Robux package",
      quantityLabel: "25 Robux",
      amountValue: 25,
      priceBdt: 70,
      imageUrl: "",
      available: true,
      active: true,
      featured: true,
      displayOrder: 1,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    ownerDb.collection("bonus_offers").doc("drop_10").set({
      title: "10th Day Robux Drop",
      description: "Special bonus drop",
      productId: "robux_100",
      productName: "100 Robux",
      imageUrl: "",
      originalPriceBdt: 220,
      discountedPriceBdt: 199,
      dropDayOfMonth: 10,
      startDate: pastDate(60000),
      endDate: new Date(Date.now() + 86400000),
      active: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    ownerDb.collection("app_settings").doc("store_config").set({
      bkashNumber: "01354246861",
      nagadNumber: "01354246861",
      paymentInstructions: "Send Money via bKash or Nagad",
      supportPhone: "01354246861",
      whatsappCommunityUrl: "https://chat.whatsapp.com/HrVQr70upFb5fNqsTM5w32",
      supportInfo: "24/7 Gamer Support",
      maintenanceMode: false,
      maintenanceMessage: "",
      announcementBanner: "Welcome to MSR BD Game Top-Up!",
      silverThreshold: 1000,
      goldThreshold: 5000,
      platinumThreshold: 12300,
      rewardDescription: "1 Point per 1 BDT verified spend",
      privacyPolicyText: "Privacy policy",
      termsText: "Terms and conditions",
      appDownloadUrl: "https://ais-pre-oc3kvhkv733pmjlr6xalk4-836236406937.asia-northeast1.run.app",
      updatedAt: now,
    })
  );
});

test("Customer profile: self create/update safe fields succeeds; points/role tampering and listing other users fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@example.com",
    email_verified: true,
  }).firestore();
  const now = pastDate();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Gamer",
      email: "alice@example.com",
      photoUrl: "",
      points: 0,
      totalVerifiedSpending: 0,
      membershipTier: "STANDARD",
      role: "customer",
      favoriteProductIds: [],
      createdAt: now,
      updatedAt: now,
    })
  );

  // Safe favorites update succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      favoriteProductIds: ["robux_25"],
      updatedAt: pastDate(1000),
    })
  );

  // Tampering with points or role fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      points: 99999,
      updatedAt: pastDate(1000),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      role: "owner",
      updatedAt: pastDate(1000),
    })
  );

  // Listing all users is forbidden for customers
  await assertFails(aliceDb.collection("users").get());
});

test("Orders: customer can create PENDING order and query own orders; cannot auto-approve or read Bob's order", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@example.com",
    email_verified: true,
  }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, {
    email: "bob@example.com",
    email_verified: true,
  }).firestore();
  const ownerDb = testEnv.authenticatedContext(OWNER_UID, {
    email: OWNER_EMAIL,
    email_verified: true,
  }).firestore();

  const now = pastDate();
  const validOrder = {
    orderCode: "MSR-20261119-1001",
    userId: ALICE_UID,
    customerName: "Alice Gamer",
    customerEmail: "alice@example.com",
    gameId: "roblox",
    gameName: "Roblox",
    categoryName: "Roblox Robux",
    productId: "robux_25",
    productName: "25 Robux",
    quantityLabel: "25 Robux",
    unitPriceBdt: 70,
    totalAmountBdt: 70,
    targetAccountLabel: "Roblox Username",
    targetAccountInput: "AliceRbx99",
    paymentMethod: "bKash",
    paymentRecipientNumber: "01354246861",
    paymentSenderNumber: "01711000000",
    transactionId: "TXN998877",
    paymentConfirmed: true,
    status: "PENDING",
    adminNote: "",
    pointsAwarded: false,
    createdAt: now,
    updatedAt: now,
  };

  await assertSucceeds(aliceDb.collection("orders").doc("order_1").set(validOrder));

  // Nagad payment method also succeeds
  await assertSucceeds(
    aliceDb.collection("orders").doc("order_nagad").set({
      ...validOrder,
      orderCode: "MSR-20261119-1002",
      paymentMethod: "Nagad",
      transactionId: "NGD998877",
    })
  );

  // Unsupported payment method is rejected by rules
  await assertFails(
    aliceDb.collection("orders").doc("order_bad_method").set({
      ...validOrder,
      paymentMethod: "Rocket",
    })
  );

  // Customer cannot create an order already marked COMPLETED
  await assertFails(
    aliceDb.collection("orders").doc("order_hack").set({
      ...validOrder,
      status: "COMPLETED",
      pointsAwarded: true,
    })
  );

  // Customer query aligned with userId succeeds; unfiltered list fails
  await assertSucceeds(aliceDb.collection("orders").where("userId", "==", ALICE_UID).get());
  await assertFails(aliceDb.collection("orders").get());

  // Cross-user access denied
  await assertFails(bobDb.collection("orders").doc("order_1").get());

  // Customer cannot approve their own order
  await assertFails(
    aliceDb.collection("orders").doc("order_1").update({
      status: "COMPLETED",
      pointsAwarded: true,
      updatedAt: pastDate(500),
    })
  );

  // Owner can approve & complete order
  await assertSucceeds(
    ownerDb.collection("orders").doc("order_1").update({
      status: "COMPLETED",
      adminNote: "Verified bKash TxID",
      pointsAwarded: true,
      updatedAt: pastDate(500),
    })
  );
});

test("Catalog queries: customer filtered active==true queries succeed; unfiltered queries fail", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    const now = pastDate();
    await db.collection("games").doc("roblox").set({
      name: "Roblox",
      slug: "roblox",
      description: "Roblox",
      imageUrl: "",
      themeColorHex: "#1E88E5",
      active: true,
      featured: true,
      displayOrder: 1,
      createdAt: now,
      updatedAt: now,
    });
    await db.collection("games").doc("freefire_hidden").set({
      name: "Free Fire",
      slug: "free-fire",
      description: "Future",
      imageUrl: "",
      themeColorHex: "#FF5722",
      active: false,
      featured: false,
      displayOrder: 2,
      createdAt: now,
      updatedAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("games").where("active", "==", true).get());
  await assertFails(aliceDb.collection("games").get());
  await assertFails(aliceDb.collection("games").doc("freefire_hidden").get());
});

test("Owner order messages: atomic customer batch creation succeeds; customer read/forge fails; owner read/mark-read succeeds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@example.com",
    email_verified: true,
  }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, {
    email: "bob@example.com",
    email_verified: true,
  }).firestore();
  const ownerDb = testEnv.authenticatedContext(OWNER_UID, {
    email: "MdSeamRahman2525@gmail.com",
    email_verified: true,
  }).firestore();

  const now = pastDate();
  const orderId = "order_batch_1";

  // Batch create order + owner_order_message together
  const batch = aliceDb.batch();
  batch.set(aliceDb.collection("orders").doc(orderId), {
    orderCode: "MSR-20261119-2002",
    userId: ALICE_UID,
    customerName: "Alice Gamer",
    customerEmail: "alice@example.com",
    gameId: "roblox",
    gameName: "Roblox",
    categoryName: "Robux",
    productId: "robux_100",
    productName: "100 Robux",
    quantityLabel: "100 Robux",
    unitPriceBdt: 165,
    totalAmountBdt: 165,
    targetAccountLabel: "Roblox Username",
    targetAccountInput: "AliceRbx",
    paymentMethod: "bKash",
    paymentRecipientNumber: "01354246861",
    paymentSenderNumber: "01711111111",
    transactionId: "BK123456",
    paymentConfirmed: true,
    status: "PENDING",
    adminNote: "",
    pointsAwarded: false,
    createdAt: now,
    updatedAt: now,
  });
  batch.set(aliceDb.collection("owner_order_messages").doc(orderId), {
    orderId,
    orderCode: "MSR-20261119-2002",
    customerUid: ALICE_UID,
    customerName: "Alice Gamer",
    customerEmail: "alice@example.com",
    gameName: "Roblox",
    productName: "100 Robux",
    totalAmountBdt: 165,
    targetAccountLabel: "Roblox Username",
    targetAccountInput: "AliceRbx",
    paymentSenderNumber: "01711111111",
    transactionId: "BK123456",
    read: false,
    createdAt: now,
  });
  await assertSucceeds(batch.commit());

  // Forging owner_order_message without a backing order fails
  await assertFails(
    aliceDb.collection("owner_order_messages").doc("orphan_msg").set({
      orderId: "orphan_msg",
      orderCode: "MSR-FAKE-1",
      customerUid: ALICE_UID,
      customerName: "Alice",
      customerEmail: "alice@example.com",
      gameName: "Roblox",
      productName: "1000 Robux",
      totalAmountBdt: 1420,
      targetAccountLabel: "Roblox Username",
      targetAccountInput: "AliceRbx",
      paymentSenderNumber: "01711111111",
      transactionId: "FAKE",
      read: false,
      createdAt: now,
    })
  );

  // Customers cannot read or list owner_order_messages
  await assertFails(aliceDb.collection("owner_order_messages").doc(orderId).get());
  await assertFails(bobDb.collection("owner_order_messages").get());

  // Verified owner (even with mixed-case Gmail token) can read and mark read
  await assertSucceeds(ownerDb.collection("owner_order_messages").get());
  await assertSucceeds(
    ownerDb.collection("owner_order_messages").doc(orderId).update({ read: true })
  );
});
