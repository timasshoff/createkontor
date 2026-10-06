package com.timder.kontor.registry;

import com.timder.kontor.game.block.company.CompanyBlockSupport;

import static com.timder.kontor.registry.KontorRegistries.REGISTRATE;

public class KontorLanguage {

    static {
        REGISTRATE.addRawLang("createkontor.configuration.title", "Create: Kontor Configs");
        REGISTRATE.addRawLang("itemGroup.createkontor", "Create: Kontor");

        REGISTRATE.addRawLang(CompanyBlockSupport.MESSAGE_BOUND, "§7Connected to %s");
        REGISTRATE.addRawLang(CompanyBlockSupport.MESSAGE_NOT_IN_COMPANY, "§cYou are not a member of any company. The block stays unconnected.");
        REGISTRATE.addRawLang(CompanyBlockSupport.MESSAGE_UNBOUND, "§cThe block is not connected to a company.");
        REGISTRATE.addRawLang(CompanyBlockSupport.MESSAGE_COMPANY_GONE, "§cThe company of this block no longer exists.");
        REGISTRATE.addRawLang(CompanyBlockSupport.MESSAGE_NOT_MEMBER, "§cThis block belongs to another company.");
        REGISTRATE.addRawLang(CompanyBlockSupport.MESSAGE_STATUS, "§7Company: %s");
        REGISTRATE.addRawLang("message.createkontor.already_member", "§cYou are already a member of a company.");
        REGISTRATE.addRawLang("message.createkontor.employee_desk.vacant", "§cThis desk is not staffed.");
        REGISTRATE.addRawLang("message.createkontor.shipping_exit.limit", "§cLimit reached: Legal form allows at most %s §cbound shipping exits.");
        REGISTRATE.addRawLang("message.createkontor.kontor_desk.limit", "§cThis company already has a Kontor Desk.");
        REGISTRATE.addRawLang("message.createkontor.kontor_desk.request_accepting_failed", "§cAccepting request %s §cfailed: %s");
        REGISTRATE.addRawLang("message.createkontor.upgrade.completed", "§7The company %s §7has advanced to %s§7.");
        REGISTRATE.addRawLang("message.createkontor.upgrade.resting", "§7The application of %s §7for %s §7is being processed, but the net worth is too low. It rests for up to %s §7day(s). Raise the net worth to at least %s§7.");
        REGISTRATE.addRawLang("message.createkontor.upgrade.cancelled", "§cThe application of %s §cfor %s §cwas cancelled because the company has no lawyer anymore. The fee of %s §cwas refunded.");
        REGISTRATE.addRawLang("message.createkontor.upgrade.rejected", "§cThe application of %s §cfor %s §cwas rejected: the net worth was below %s§c for too long. The fee of %s §cis lost.");
        REGISTRATE.addRawLang("message.createkontor.buyer_desk.buy.success", "%s package(s) are being delivered!");

        REGISTRATE.addRawLang(CompanyBlockSupport.GOGGLE_COMPANY_BLOCK, "Company");
        REGISTRATE.addRawLang(CompanyBlockSupport.GOGGLE_COMPANY_BLOCK_UNBOUND, "Not bound to any company.");
        REGISTRATE.addRawLang("goggle.createkontor.employee_desk.role", "Role: %s");
        REGISTRATE.addRawLang("goggle.createkontor.employee_desk.seat_empty", "Seat: empty");
        REGISTRATE.addRawLang("goggle.createkontor.employee_desk.employed", "Employed: %s");
        REGISTRATE.addRawLang("goggle.createkontor.employee_desk.salary", "Daily salary: %s");
        REGISTRATE.addRawLang("goggle.createkontor.employee_desk.vacant", "Not staffed.");
        REGISTRATE.addRawLang("goggle.createkontor.employee_desk.no_free_slot", "Cannot hire: the legal form allows no more employees.");
        REGISTRATE.addRawLang("goggle.createkontor.employee_desk.not_operational", "Cannot hire: the company is in payment difficulties.");
        REGISTRATE.addRawLang("goggle.createkontor.employee_desk.not_affordable", "Cannot hire: the company cannot afford the hiring bonus.");
        REGISTRATE.addRawLang("goggle.createkontor.employee_desk.offer_salary", "Salary: %s per day");
        REGISTRATE.addRawLang("goggle.createkontor.employee_desk.offer_bonus", "Hiring bonus: %s once");
        REGISTRATE.addRawLang("goggle.createkontor.employee_desk.inactive", "Paused");
        REGISTRATE.addRawLang("goggle.createkontor.buyer_desk.postboxes", "Connected postboxes: %s");
        REGISTRATE.addRawLang("goggle.createkontor.buyer_desk.no_postbox", "No postbox connected");
        REGISTRATE.addRawLang("goggle.createkontor.buyer_desk.connect_hint", "R-Click the desk with a postbox, then place it nearby");
        REGISTRATE.addRawLang("goggle.createkontor.buyer_desk.pending", "Packages waiting: %s");

        REGISTRATE.addRawLang("ui.createkontor.chart.no_data.detailed", "There is no data for the chart \"%s\" yet. You can try again later.");
        REGISTRATE.addRawLang("ui.createkontor.chart.no_data.short", "There is no data for this chart yet. You can try again later.");

        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.business_fee.info", "§7A business fee of %s is charged every day and debited from the company's bank account.");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.deposit.info", "§7Every company receives a free deposit of %s that does not need to be payed back.");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.founders_loan.info", "§7Additionally, you can take a founders loan of %s with %s free days before you need to repay it.");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.founding", "Found company");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.founding_title", "Found a new company");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.founded", "%s has been founded!");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.company_name", "Company Name");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.overview", "Overview");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.requests_orders", "Requests & Orders");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.account", "Bank Account");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.balance", "Balance");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.cost_structure", "Cost Structure");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.bookings", "Bookings");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.open_loans", "Open Loans");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.markets", "Markets");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.open_orders", "Open Orders: %s");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.total_bookings_visible", "Visible Bookings: %s");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.current_overdraft_limit", "Overdraft Limit: %s");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.net_worth", "Net Worth: %s");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.bookings_count", "Bookings (%s)");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.loans_count", "Open Loans (%s)");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.loan.principal", "%s: %s");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.loan.outstanding", "Outstanding: %s");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.loan.rate", "Daily Interest Rate: %s");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.loan.repayment", "Daily Repayment: %s");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.loan.term_days", "Term Days: %s");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.loan.free_days", "Free Days: %s");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.requests", "Requests");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.requests_count", "Requests (%s/%s)");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.requests.empty", "Incoming requests will be visible here. To receive requests for a products you need to own a license for that product and participate in the products market.");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.request.title", "%s for %s §8(#%s§8)");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.request.unit_price", "Unit price: %s");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.request.offer_time", "Offer stands for %s.");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.request.deadline", "Time to deliver after accepting: %s");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.request.accept", "Accept request");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.orders", "Order Book");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.orders_count", "Order Book (%s/%s)");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.orders.empty", "Accepted requests will be visible here.");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.order.delivery_time", "Remaining time to deliver: %s.");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.order.grace_period", "Deadline reached! Fulfill this order now to receive a partial payout!");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.markets_count", "Licensed Markets (%s)");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.markets.empty", "No licensed markets found. Buy a license for a market through a lawyer.");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.markets.no_result", "No matching market found.");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.markets.search", "Search licensed markets");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.markets.participate", "Participate and receive requests");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.markets.participate_short", "Active");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.markets.list_price", "Your current list price:");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.markets.market_price", "Current Market Price: %s");

        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.legal_form", "Legal Form");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.legal_form.upgrade_in_progress", "Legal form upgrade in progress...");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.legal_form.no_upgrade", "No legal form upgrade in progress.");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.legal_form.no_next_legal_form", "No next legal form available :(");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.legal_form.upgrade_to", "Upgrade legal form to");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.legal_form.stats", "Legal Form Stats");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.legal_form.application_fee", "Application Fee: %s");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.legal_form.upgrade_requirements", "Upgrade Requirements");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.legal_form.all_requirements_met", "All requirements met! Ready to upgrade.");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.legal_form.not_all_requirements_met", "Not all requirements are met.");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.legal_form.upgrade_to_in_progress", "Upgrade to %s in progress.");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.legal_form.waiting_for_net_worth", "§cApplication is resting because the net worth is too low. Raise the net worth to at least %s§c.");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.legal_form.resting_days_remaining", "§cRemaining days before application is cancelled: %s");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.legal_form.apply_for_upgrade", "Apply for upgrade (application fee: %s)");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.legal_form.upgrade_info", "After applying you will need to hold the minimum net worth for the duration of the upgrade. If your net worth gets too low the upgrade application will rest for a few days before it gets rejected and you lose your application fee.");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.legal_form.upgrade_lawyer_lost", "Your company has to have a lawyer for the entire duration of the upgrade. If the lawyer gets lost, the application will be cancelled and the application fee will be refunded.");

        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.license_catalog", "License Catalog");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.license_catalog_count", "License Catalog (%s)");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.license_catalog.daily_fee", "Daily Fee");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.license_catalog.revenue_share", "Revenue Share");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.license_catalog.application_fee", "Activation Fee");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.license_catalog.covered_markets", "You already have licenses for these markets:");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.license_catalog.already_held", "You already own this license.");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.license_catalog.unknown_license", "License unknown.");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.license_catalog.legal_level_too_low", "Legal level too low.");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.license_catalog.limit_reached", "Max. licenses reached.");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.license_catalog.not_operational", "Payment difficulties");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.license_catalog.cannot_afford", "Too expensive");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.license_catalog.buy", "Buy license");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.license_catalog.reactivate", "Reactivate cancelled license");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.license_catalog.no_result", "No matching license found.");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.license_catalog.search", "Search license catalog");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.licenses", "Licenses");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.licenses_count", "Licenses (%s)");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.licenses.empty", "Your company does not hold any licenses. Buy a license using the license catalog.");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.licenses.daily_fee", "Daily Fee: %s");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.licenses.revenue_share", "Revenue Share: %s");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.licenses.cancel", "Cancel License");
        REGISTRATE.addRawLang("ui.createkontor.lawyer_desk.licenses.cancelled", "This license has been cancelled and will be removed from this list tomorrow.");

        REGISTRATE.addRawLang("ui.createkontor.buyer_desk.catalog", "Catalog");
        REGISTRATE.addRawLang("ui.createkontor.buyer_desk.catalog_count", "Catalog (%s)");
        REGISTRATE.addRawLang("ui.createkontor.buyer_desk.quote", "Cart");
        REGISTRATE.addRawLang("ui.createkontor.buyer_desk.catalog.search", "Search catalog");
        REGISTRATE.addRawLang("ui.createkontor.buyer_desk.catalog.no_result", "No matching offers found");
        REGISTRATE.addRawLang("ui.createkontor.buyer_desk.catalog.price", "%s per unit");
        REGISTRATE.addRawLang("ui.createkontor.buyer_desk.catalog.raw_material", "Raw Material");
        REGISTRATE.addRawLang("ui.createkontor.buyer_desk.catalog.product", "Product");
        REGISTRATE.addRawLang("ui.createkontor.buyer_desk.catalog.add", "Add");
        REGISTRATE.addRawLang("ui.createkontor.buyer_desk.quote.empty", "Empty");
        REGISTRATE.addRawLang("ui.createkontor.buyer_desk.quote.line", "%sx %s");
        REGISTRATE.addRawLang("ui.createkontor.buyer_desk.quote.total", "Total: %s");
        REGISTRATE.addRawLang("ui.createkontor.buyer_desk.quote.address", "Package Adresse");
        REGISTRATE.addRawLang("ui.createkontor.buyer_desk.quote.address.tooltip", "(Optional) Delivered packages will be addressed to this value");
        REGISTRATE.addRawLang("ui.createkontor.buyer_desk.quote.buy", "Buy");
        REGISTRATE.addRawLang("ui.createkontor.buyer_desk.quote.remove", "Remove");
        REGISTRATE.addRawLang("ui.createkontor.buyer_desk.no_postbox", "No connected postbox");

        REGISTRATE.addRawLang("enum.createkontor.liquidity.normal", "§2Normal Liquidity");
        REGISTRATE.addRawLang("enum.createkontor.liquidity.illiquidity", "§4Illiquidity");

        REGISTRATE.addRawLang("enum.createkontor.booking_kind.business_license", "Business License");
        REGISTRATE.addRawLang("enum.createkontor.booking_kind.flat_license", "Flat Product License");
        REGISTRATE.addRawLang("enum.createkontor.booking_kind.turnover_license", "Turnover Product License");
        REGISTRATE.addRawLang("enum.createkontor.booking_kind.application_fee", "License Application Fee");
        REGISTRATE.addRawLang("enum.createkontor.booking_kind.feed_in_license", "SU Feed-in License");
        REGISTRATE.addRawLang("enum.createkontor.booking_kind.salary", "Salary");
        REGISTRATE.addRawLang("enum.createkontor.booking_kind.hire_bonus", "Hire Bonus");
        REGISTRATE.addRawLang("enum.createkontor.booking_kind.power", "Power");
        REGISTRATE.addRawLang("enum.createkontor.booking_kind.storage", "Storage");
        REGISTRATE.addRawLang("enum.createkontor.booking_kind.purchase", "Purchase");
        REGISTRATE.addRawLang("enum.createkontor.booking_kind.interest", "Interest");
        REGISTRATE.addRawLang("enum.createkontor.booking_kind.contract_penalty", "Contract Penalty");
        REGISTRATE.addRawLang("enum.createkontor.booking_kind.upgrade_fee", "Legal Form Upgrade Fee");
        REGISTRATE.addRawLang("enum.createkontor.booking_kind.order_revenue", "Order Revenue");
        REGISTRATE.addRawLang("enum.createkontor.booking_kind.partial_payment", "Partial Order Revenue");
        REGISTRATE.addRawLang("enum.createkontor.booking_kind.feed_in_revenue", "SU Feed-in Revenue");
        REGISTRATE.addRawLang("enum.createkontor.booking_kind.deposit", "Deposit");
        REGISTRATE.addRawLang("enum.createkontor.booking_kind.loan_payout", "Loan Payout");
        REGISTRATE.addRawLang("enum.createkontor.booking_kind.loan_repayment", "Loan Repayment");

        REGISTRATE.addRawLang("enum.createkontor.booking_category.cost", "Cost");
        REGISTRATE.addRawLang("enum.createkontor.booking_category.revenue", "Revenue");
        REGISTRATE.addRawLang("enum.createkontor.booking_category.financing", "Financing");

        REGISTRATE.addRawLang("enum.createkontor.upgrade_criterion.net_worth", "Net Worth (Required Net Worth + Application Fee)");
        REGISTRATE.addRawLang("enum.createkontor.upgrade_criterion.fulfilled_orders", "Fulfilled Orders");
        REGISTRATE.addRawLang("enum.createkontor.upgrade_criterion.reputation", "Reputation");
        REGISTRATE.addRawLang("enum.createkontor.upgrade_criterion.liquidity", "No payment difficulties");

        REGISTRATE.addRawLang("enum.createkontor.license_cancel_result.already_cancelled", "License already cancelled.");
        REGISTRATE.addRawLang("enum.createkontor.license_cancel_result.not_held", "Company does not own this license.");

        REGISTRATE.addRawLang("enum.createkontor.acquire_result.already_held", "Company already holds this license.");
        REGISTRATE.addRawLang("enum.createkontor.acquire_result.unknown_license", "License does not exist.");
        REGISTRATE.addRawLang("enum.createkontor.acquire_result.legal_level_too_low", "Legal level too low.");
        REGISTRATE.addRawLang("enum.createkontor.acquire_result.limit_reached", "Company already reached max. licenses.");
        REGISTRATE.addRawLang("enum.createkontor.acquire_result.not_operational", "Company is in payment difficulties.");
        REGISTRATE.addRawLang("enum.createkontor.acquire_result.cannot_afford", "Company cannot afford this license.");

        REGISTRATE.addRawLang("enum.createkontor.purchase_status.not_operational", "Company is in payment difficulties.");
        REGISTRATE.addRawLang("enum.createkontor.purchase_status.insufficient_funds", "Company cannot afford this delivery.");

        REGISTRATE.addRawLang("legal_forms.createkontor.sole_proprietorship", "§7Sole Proprietorship");
        REGISTRATE.addRawLang("legal_forms.createkontor.partnership", "§bPartnership");
        REGISTRATE.addRawLang("legal_forms.createkontor.limited_company", "§5Limited Company");
        REGISTRATE.addRawLang("legal_forms.createkontor.public_company", "§6Public Company");

        REGISTRATE.addRawLang("license.createkontor.oak_bundle", "Oak Bundle");
        REGISTRATE.addRawLang("license.createkontor.spruce_bundle", "Spruce Bundle");
        REGISTRATE.addRawLang("license.createkontor.birch_bundle", "Birch Bundle");
        REGISTRATE.addRawLang("license.createkontor.jungle_bundle", "Jungle Bundle");
        REGISTRATE.addRawLang("license.createkontor.acacia_bundle", "Acacia Bundle");
        REGISTRATE.addRawLang("license.createkontor.dark_oak_bundle", "Dark Oak Bundle");
        REGISTRATE.addRawLang("license.createkontor.mangrove_bundle", "Mangrove Bundle");
        REGISTRATE.addRawLang("license.createkontor.cherry_bundle", "Cherry Bundle");
        REGISTRATE.addRawLang("license.createkontor.nether_wood_bundle", "Nether Wood Bundle");
        REGISTRATE.addRawLang("license.createkontor.nether_wood_bundle_turnover", "Nether Wood Bundle");
        REGISTRATE.addRawLang("license.createkontor.cobblestone_bundle", "Cobblestone Bundle");
        REGISTRATE.addRawLang("license.createkontor.stone_bundle", "Stone Bundle");
        REGISTRATE.addRawLang("license.createkontor.small_andesite_bundle", "Small Andesite Bundle");
        REGISTRATE.addRawLang("license.createkontor.large_andesite_bundle", "Large Andesite Bundle");
        REGISTRATE.addRawLang("license.createkontor.basic_mechanical_bundle", "Basic Mechanical Bundle");
        REGISTRATE.addRawLang("license.createkontor.metal_bundle_1", "Metal Bundle I");
        REGISTRATE.addRawLang("license.createkontor.metal_bundle_2", "Metal Bundle II");
        REGISTRATE.addRawLang("license.createkontor.sheet_metal_bundle", "Sheet Metal Bundle");
        REGISTRATE.addRawLang("license.createkontor.casing_bundle", "Casing Bundle");
        REGISTRATE.addRawLang("license.createkontor.dye_bundle_1", "Dye Bundle I");
        REGISTRATE.addRawLang("license.createkontor.dye_bundle_2", "Dye Bundle II");
        REGISTRATE.addRawLang("license.createkontor.generated", "%s License");

        REGISTRATE.addRawLang("employee_role.createkontor.lawyer", "Lawyer");
        REGISTRATE.addRawLang("employee_role.createkontor.buyer", "Buyer");

        REGISTRATE.addRawLang("economy.createkontor.current_day", "§7Day %s");

        REGISTRATE.addRawLang("loan.createkontor.bank", "Bank Loan");
        REGISTRATE.addRawLang("loan.createkontor.founder", "Founder's Loan");
        REGISTRATE.addRawLang("loan.createkontor.principal", "Principal: %s");

        REGISTRATE.addRawLang("chart.createkontor.d", "d");
        REGISTRATE.addRawLang("chart.createkontor.day", "Day ");
        REGISTRATE.addRawLang("chart.createkontor.today", "Today");
        REGISTRATE.addRawLang("chart.createkontor.other", "Other");

        REGISTRATE.addRawLang("chart.createkontor.balance.title", "Balance");
        REGISTRATE.addRawLang("chart.createkontor.balance.series.balance", "Balance");
        REGISTRATE.addRawLang("chart.createkontor.balance.reference.overdraft", "Overdraft Limit");
        REGISTRATE.addRawLang("chart.createkontor.balance.bookings", " Bookings");

        REGISTRATE.addRawLang("chart.createkontor.revenue_result.title", "Daily Revenue & Daily Result");
        REGISTRATE.addRawLang("chart.createkontor.revenue_result.series.revenue", "Revenue");
        REGISTRATE.addRawLang("chart.createkontor.revenue_result.series.result", "Result");

        REGISTRATE.addRawLang("chart.createkontor.cost_structure.title", "Cost Structure");

        REGISTRATE.addRawLang("toast.createkontor.error.title", "An error occurred:");
        REGISTRATE.addRawLang("toast.createkontor.info.title", "Information:");
        REGISTRATE.addRawLang("toast.createkontor.success.title", "Success:");

        REGISTRATE.addRawLang("block.createkontor.kontor_desk.tooltip.summary", "Allows _founding_ or _management_ of a company.");
        REGISTRATE.addRawLang("block.createkontor.kontor_desk.tooltip.control1", "When R-Clicked");
        REGISTRATE.addRawLang("block.createkontor.kontor_desk.tooltip.action1", "Opens the _Kontor Desk_ interface.");

        REGISTRATE.addRawLang("block.createkontor.lawyer_desk.tooltip.summary", "Staffs the company with a lawyer.");
        REGISTRATE.addRawLang("block.createkontor.lawyer_desk.tooltip.condition1", "When a Villager sits next to it");
        REGISTRATE.addRawLang("block.createkontor.lawyer_desk.tooltip.behaviour1", "The villager is _hired_ as a lawyer. You need to pay a one-time _hiring bonus_.");
        REGISTRATE.addRawLang("block.createkontor.lawyer_desk.tooltip.control1", "When R-Clicked");
        REGISTRATE.addRawLang("block.createkontor.lawyer_desk.tooltip.action1", "Opens the _Lawyer's Desk_ interface.");

        REGISTRATE.addRawLang("block.createkontor.buyer_desk.tooltip.summary", "Staffs the company with a buyer.");
        REGISTRATE.addRawLang("block.createkontor.buyer_desk.tooltip.condition1", "When a Villager sits next to it");
        REGISTRATE.addRawLang("block.createkontor.buyer_desk.tooltip.behaviour1", "The villager is _hired_ as a buyer. You need to pay a one-time _hiring bonus_.");
        REGISTRATE.addRawLang("block.createkontor.buyer_desk.tooltip.control1", "When R-Clicked with a Postbox");
        REGISTRATE.addRawLang("block.createkontor.buyer_desk.tooltip.action1", "The next _Postbox_ you place nearby is _connected_ to this desk. Bought goods arrive there.");
    }

    static void touch() {
    }

    private KontorLanguage() {
    }
}
