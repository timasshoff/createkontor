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
        REGISTRATE.addRawLang("message.createkontor.new_day", "§7Markets have closed and reopened with new prices. Trading day %s §7starts!");
        REGISTRATE.addRawLang("message.createkontor.already_member", "§cYou are already a member of a company.");
        REGISTRATE.addRawLang("message.createkontor.employee_desk.vacant", "§cThis desk is not staffed.");
        REGISTRATE.addRawLang("message.createkontor.shipping_exit.limit", "§cLimit reached: Legal form allows at most %s §cbound shipping exits.");
        REGISTRATE.addRawLang("message.createkontor.kontor_desk.limit", "§cThis company already has a Kontor Desk.");
        REGISTRATE.addRawLang("message.createkontor.kontor_desk.request_accepting_failed", "§cAccepting request %s §cfailed: %s");
        REGISTRATE.addRawLang("message.createkontor.logistics_manager_desk.limit", "§cThis company already has a Logistics Manager's Desk.");
        REGISTRATE.addRawLang("message.createkontor.logistics.connected", "§7%s §7is connected to a logistics network. Stored goods cost a daily storage fee.");
        REGISTRATE.addRawLang("message.createkontor.logistics.disconnected", "§c%s §cis no longer connected to a logistics network: %s §cStorage fee booked: %s");
        REGISTRATE.addRawLang("message.createkontor.logistics.reason.no_employee", "The logistics manager's desk is not staffed.");
        REGISTRATE.addRawLang("message.createkontor.logistics.reason.no_link", "No Stock Link is attached to the desk.");
        REGISTRATE.addRawLang("message.createkontor.logistics.reason.several_links", "More than one Stock Link is attached to the desk.");
        REGISTRATE.addRawLang("message.createkontor.logistics.reason.link_changed", "The Stock Link was tuned to another network.");
        REGISTRATE.addRawLang("message.createkontor.logistics.reason.legal_form_not_allowed", "The legal form does not allow a logistics network.");
        REGISTRATE.addRawLang("message.createkontor.logistics.reason.network_used_by_other_company", "Another company uses this network.");
        REGISTRATE.addRawLang("message.createkontor.logistics.reason.network_owner_not_member", "The owner of the network is not a member of the company.");
        REGISTRATE.addRawLang("message.createkontor.logistics.reason.desk_removed", "The desk was removed.");
        REGISTRATE.addRawLang("message.createkontor.bankruptcy.not_confirmed", "§cUnderstanding of bankruptcy not confirmed.");
        REGISTRATE.addRawLang("message.createkontor.bankruptcy.declared", "Bankruptcy declared. The company %s has been deleted.");
        REGISTRATE.addRawLang("message.createkontor.upgrade.completed", "§7The company %s §7has advanced to %s§7.");
        REGISTRATE.addRawLang("message.createkontor.upgrade.resting", "§7The application of %s §7for %s §7is being processed, but the net worth is too low. It rests for up to %s §7day(s). Raise the net worth to at least %s§7.");
        REGISTRATE.addRawLang("message.createkontor.upgrade.cancelled", "§cThe application of %s §cfor %s §cwas cancelled because the company has no lawyer anymore. The fee of %s §cwas refunded.");
        REGISTRATE.addRawLang("message.createkontor.upgrade.rejected", "§cThe application of %s §cfor %s §cwas rejected: the net worth was below %s§c for too long. The fee of %s §cis lost.");
        REGISTRATE.addRawLang("message.createkontor.buyer_desk.buy.success", "%s package(s) are being delivered!");
        REGISTRATE.addRawLang("message.createkontor.manager_invite.offline", "§cThe player %s §cis offline or does not exist.");
        REGISTRATE.addRawLang("message.createkontor.manager_invite.in_company", "§c%s is already a member of a company.");
        REGISTRATE.addRawLang("message.createkontor.manager_invite.accept", "[Accept]");
        REGISTRATE.addRawLang("message.createkontor.manager_invite.accept_hover", "Click to accept invitation.");
        REGISTRATE.addRawLang("message.createkontor.manager_invite.text", "%s invites you to become a manager of %s.");
        REGISTRATE.addRawLang("message.createkontor.manager_invite.sent", "Invitation sent to %s");
        REGISTRATE.addRawLang("message.createkontor.manager_invite.joined", "%s is now a manager of %s.");
        REGISTRATE.addRawLang("message.createkontor.manager_invite.already_in_company", "You are already a member of a company.");
        REGISTRATE.addRawLang("message.createkontor.manager_fire.fired", "%s §chas fired you. You are no longer a member of the company.");
        REGISTRATE.addRawLang("message.createkontor.manager_fire.done", "Successfully fired.");
        REGISTRATE.addRawLang("message.createkontor.leave.left", "%s left the company.");
        REGISTRATE.addRawLang("message.createkontor.leave.done", "Successfully left %s.");

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
        REGISTRATE.addRawLang("goggle.createkontor.logistics_manager_desk.status", "Logistics network: %s");
        REGISTRATE.addRawLang("goggle.createkontor.logistics_manager_desk.ordering", "Ordering: %s");

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
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.manage", "Manage");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.manage_title", "Manage Company");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.managers", "Managers");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.managers.name", "Player name");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.managers.invite", "Invite");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.managers.owner", "Owner");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.managers.manager", "Manager");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.managers.fire", "Fire");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.managers.leave", "Leave Company");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.rewards", "Rewards");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.bankruptcy", "Bankruptcy");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.bankruptcy.explanation", "When your company is §4illiquid §rfor %s days and thus §cinsolvent§r, you can declare bankruptcy.");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.bankruptcy.status", "Current Liquidity: %s");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.bankruptcy.disclaimer", "Declaring bankruptcy will permanently §cdelete §ryour company and allow you to start over with a new company. It is §cnot possible §rto reverse this or restore a deleted company. A new company will start at the zero exactly like this company did (lowest legal level, founders deposit + option of taking founders loan).");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.bankruptcy.toggle", "I understand what bankruptcy means and know that the effects are not reversible.");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.bankruptcy.button", "Declare bankruptcy and delete company");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.bankruptcy.not_insolvent", "Your company is not insolvent and you cannot declare bankruptcy.");
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
        REGISTRATE.addRawLang("ui.createkontor.buyer_desk.quote.address", "Package Address");
        REGISTRATE.addRawLang("ui.createkontor.buyer_desk.quote.address.tooltip", "(Optional) Delivered packages will be addressed to this value");
        REGISTRATE.addRawLang("ui.createkontor.buyer_desk.quote.buy", "Buy");
        REGISTRATE.addRawLang("ui.createkontor.buyer_desk.quote.remove", "Remove");
        REGISTRATE.addRawLang("ui.createkontor.buyer_desk.no_postbox", "No connected postbox");

        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.markets", "Markets");
        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.raw_materials", "Raw Materials");
        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.select_market", "Select a market");
        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.select_raw_market", "Select a raw material");
        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.licensed_candidate", "%s §7(Licensed)");
        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.price_tab", "Price & Competitors");
        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.demand_overflow_tab", "Demand & Overflow");
        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.participation_tab", "Participation");
        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.no_participation", "You do not participate in this market.");
        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.current_price", "Current Price: %s §7(%s§7)");
        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.current_price.tooltip", "§7The current market price of this item");
        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.current_competitors", "Current Competitors: %s");
        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.current_competitors.tooltip", "§7The amount of simulated competitors.");
        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.current_demand", "Current Demand: %s §7units");
        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.current_demand.tooltip", "§7The current daily demand for this market.");
        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.expected_daily_quantity", "Expected daily quantity: %s units");
        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.expected_daily_quantity.tooltip", "§7How many items you will be §6requested §7to deliver on average per day.");
        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.own_price", "Your Price: %s §7(%s §7compared to market price)");
        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.own_price.tooltip", "§7Your current list price. §6Price and reputation §7determine your share.");
        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.reputation", "Your Reputation: %s");
        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.reputation.tooltip", "§7Your reputation in this market. Competitors have a reputation of §63.0§7. §6Price and reputation §7determine your share.");
        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.lost_requests", "Lost Requests Today: %s (open requests: %s/%s)");
        REGISTRATE.addRawLang("ui.createkontor.market_analyst_desk.lost_requests.tooltip", "§7How many requests you already §6lost §7today because your request board was full.");

        REGISTRATE.addRawLang("ui.createkontor.consultant_desk.wiki", "Wiki");
        REGISTRATE.addRawLang("ui.createkontor.consultant_desk.select_page", "Select a page on the left.");
        REGISTRATE.addRawLang("ui.createkontor.consultant_desk.no_pages", "No wiki pages are available.");
        REGISTRATE.addRawLang("ui.createkontor.consultant_desk.page_error", "This page could not be displayed. The log has details.");

        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.network_overview", "Network Overview");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.no_network", "§cNo logistics network connected");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.connected_network", "Logistics Network connected");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.total_items", "Total items in stock: %s");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.expected_cost", "Expected cost per day: %s");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.unloaded_links", "Warning: %s link(s) are currently not loaded. §7This might happen because a chunk that contains a link is currently not loaded.");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.orders", "Automatic Order Delivery");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.order_delivery_settings", "Settings");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.ordering_enabled", "Automatic Ordering");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.address", "Address");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.address.tooltip", "Packages will be sent to this address.");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.minimum_stock", "Minimum stock to keep");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.minimum_stock.tooltip", "The logistics manager keeps this many items of each product in the network and does not order them. 0 means no minimum.");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.ordering_explanation", "When enabled, the logistics manager will check your order book periodically and send missing items from the order book to a set address. He will keep track of already ordered items to avoid overflow at the target address.");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.status", "Status:");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.status.off", "Automatic order delivery is currently switched off.");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.status.not_connected", "§cThere is no storage network currently connected.");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.status.no_address", "§cThere is no address set.");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.status.waiting", "§aSwitched on and waiting for the next order book check.");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.status.nothing_to_order", "§eNothing to order: no open order, everything already covered or not enough items in stock. Trying again automatically.");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.status.ordered", "§aThe last attempt ordered items to the set address.");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.status.busy", "§cThe last attempt could not order items because the network was too busy.");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.order_delivery_overview", "Delivery Overview");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.order_delivery_overview_count", "Delivery Overview (%s)");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.order_missing", "Missing items: %s");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.order_progress.tooltip", "%s%% of the items of this order have been sent to the target address.");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.order_reset", "Reset tracked progress");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.order_reset.tooltip", "Resets the tracked delivery progress of this order back to 0%.");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.order_reset_all", "Reset tracked progress of all orders");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.order_reset_all.tooltip", "Resets the tracked delivery progress of all orders back to 0%.");
        REGISTRATE.addRawLang("ui.createkontor.logistics_manager_desk.no_orders", "The Logistics Manager does not keep track of any orders right now.");

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

        REGISTRATE.addRawLang("enum.createkontor.logistics_manager_desk_status.waiting", "Waiting");
        REGISTRATE.addRawLang("enum.createkontor.logistics_manager_desk_status.connected", "Connected");
        REGISTRATE.addRawLang("enum.createkontor.logistics_manager_desk_status.no_employee", "Not staffed");
        REGISTRATE.addRawLang("enum.createkontor.logistics_manager_desk_status.no_link", "Missing Stock Link");
        REGISTRATE.addRawLang("enum.createkontor.logistics_manager_desk_status.several_links", "Too many Stock Links");
        REGISTRATE.addRawLang("enum.createkontor.logistics_manager_desk_status.legal_form_not_allowed", "Legal Level too low");
        REGISTRATE.addRawLang("enum.createkontor.logistics_manager_desk_status.company_has_other_network", "Company has another network");
        REGISTRATE.addRawLang("enum.createkontor.logistics_manager_desk_status.network_used_by_other_company", "Network used by another company");
        REGISTRATE.addRawLang("enum.createkontor.logistics_manager_desk_status.network_owner_not_member", "Network owner is not a member");

        REGISTRATE.addRawLang("enum.createkontor.logistics_manager_desk_ordering.off", "Off");
        REGISTRATE.addRawLang("enum.createkontor.logistics_manager_desk_ordering.not_connected", "Not connected");
        REGISTRATE.addRawLang("enum.createkontor.logistics_manager_desk_ordering.no_address", "No address");
        REGISTRATE.addRawLang("enum.createkontor.logistics_manager_desk_ordering.waiting", "Waiting");
        REGISTRATE.addRawLang("enum.createkontor.logistics_manager_desk_ordering.nothing_to_order", "No orders");
        REGISTRATE.addRawLang("enum.createkontor.logistics_manager_desk_ordering.ordered", "Ordered");
        REGISTRATE.addRawLang("enum.createkontor.logistics_manager_desk_ordering.busy", "Busy");

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
        REGISTRATE.addRawLang("employee_role.createkontor.market_analyst", "Market Analyst");
        REGISTRATE.addRawLang("employee_role.createkontor.consultant", "Consultant");
        REGISTRATE.addRawLang("employee_role.createkontor.logistics_manager", "Logistics Manager");

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

        REGISTRATE.addRawLang("chart.createkontor.market_share.title", "Market Share");
        REGISTRATE.addRawLang("chart.createkontor.market_share.series.you", "You");
        REGISTRATE.addRawLang("chart.createkontor.market_share.series.you.description", "Expected share of the daily demand, based on your price and reputation.");
        REGISTRATE.addRawLang("chart.createkontor.market_share.series.other_companies", "Other Companies");
        REGISTRATE.addRawLang("chart.createkontor.market_share.series.other_companies.description", "Expected share of the daily demand of all other player-run companies, based on their price and reputation.");
        REGISTRATE.addRawLang("chart.createkontor.market_share.series.competition", "Competition");
        REGISTRATE.addRawLang("chart.createkontor.market_share.series.competition.description", "Expected share of the daily demand of all other simulated competitors.");

        REGISTRATE.addRawLang("chart.createkontor.market_price_competitors.title", "Market History");
        REGISTRATE.addRawLang("chart.createkontor.market_price_competitors.series.price", "Price");
        REGISTRATE.addRawLang("chart.createkontor.market_price_competitors.series.competitors", "Competitors");
        REGISTRATE.addRawLang("chart.createkontor.market_price_competitors.series.demand", "Demand");
        REGISTRATE.addRawLang("chart.createkontor.market_price_competitors.reference.list_price", "Your list price");

        REGISTRATE.addRawLang("chart.createkontor.market_demand_overflow.title", "Market History");
        REGISTRATE.addRawLang("chart.createkontor.market_demand_overflow.series.demand", "Demand");
        REGISTRATE.addRawLang("chart.createkontor.market_demand_overflow.series.overflow", "Unserved Demand");

        REGISTRATE.addRawLang("chart.createkontor.raw_material_price.title", "Price History");
        REGISTRATE.addRawLang("chart.createkontor.raw_material_price.series.price", "Price");

        REGISTRATE.addRawLang("chart.createkontor.cost_structure.title", "Cost Structure");

        REGISTRATE.addRawLang("chart.createkontor.stock.title", "Stock Overview");

        REGISTRATE.addRawLang("toast.createkontor.error.title", "An error occurred:");
        REGISTRATE.addRawLang("toast.createkontor.info.title", "Information:");
        REGISTRATE.addRawLang("toast.createkontor.success.title", "Success:");

        REGISTRATE.addRawLang("block.createkontor.kontor_desk.tooltip.summary", "Allows _founding_ or _management_ of a company.");
        REGISTRATE.addRawLang("block.createkontor.kontor_desk.tooltip.control1", "When R-Clicked");
        REGISTRATE.addRawLang("block.createkontor.kontor_desk.tooltip.action1", "Opens the _Kontor Desk_ interface.");

        REGISTRATE.addRawLang("block.createkontor.lawyer_desk.tooltip.summary", "Staffs the company with a _lawyer_. A lawyer handles _legal matters_ like licensing or legal form upgrades for your company.");
        REGISTRATE.addRawLang("block.createkontor.lawyer_desk.tooltip.condition1", "When a Villager sits next to it");
        REGISTRATE.addRawLang("block.createkontor.lawyer_desk.tooltip.behaviour1", "The villager is _hired_ as a lawyer. You need to pay a one-time _hiring bonus_.");
        REGISTRATE.addRawLang("block.createkontor.lawyer_desk.tooltip.control1", "When R-Clicked on Employee");
        REGISTRATE.addRawLang("block.createkontor.lawyer_desk.tooltip.action1", "Opens the _Lawyer's Desk_ interface.");

        REGISTRATE.addRawLang("block.createkontor.buyer_desk.tooltip.summary", "Staffs the company with a _buyer_. A buyer allows you to _buy goods_ from markets.");
        REGISTRATE.addRawLang("block.createkontor.buyer_desk.tooltip.condition1", "When a Villager sits next to it");
        REGISTRATE.addRawLang("block.createkontor.buyer_desk.tooltip.behaviour1", "The villager is _hired_ as a buyer. You need to pay a one-time _hiring bonus_.");
        REGISTRATE.addRawLang("block.createkontor.buyer_desk.tooltip.control1", "When R-Clicked on Employee");
        REGISTRATE.addRawLang("block.createkontor.buyer_desk.tooltip.action1", "Opens the _Buyer's Desk_ interface.");
        REGISTRATE.addRawLang("block.createkontor.buyer_desk.tooltip.control2", "When R-Clicked with a Postbox");
        REGISTRATE.addRawLang("block.createkontor.buyer_desk.tooltip.action2", "The next _Postbox_ you place nearby is _connected_ to this desk. Bought goods arrive there.");

        REGISTRATE.addRawLang("block.createkontor.market_analyst_desk.tooltip.summary", "Staffs the company with a _market analyst_. A market allows you to get detailed info and charts about markets.");
        REGISTRATE.addRawLang("block.createkontor.market_analyst_desk.tooltip.condition1", "When a Villager sits next to it");
        REGISTRATE.addRawLang("block.createkontor.market_analyst_desk.tooltip.behaviour1", "The villager is _hired_ as a market analyst. You need to pay a one-time _hiring bonus_.");
        REGISTRATE.addRawLang("block.createkontor.market_analyst_desk.tooltip.control1", "When R-Clicked on Employee");
        REGISTRATE.addRawLang("block.createkontor.market_analyst_desk.tooltip.action1", "Opens the _Market Analyst's Desk_ interface.");

        REGISTRATE.addRawLang("block.createkontor.consultant_desk.tooltip.summary", "Staffs the company with a _consultant_. A consultant explains how your company and its _systems_ work.");
        REGISTRATE.addRawLang("block.createkontor.consultant_desk.tooltip.condition1", "When a Villager sits next to it");
        REGISTRATE.addRawLang("block.createkontor.consultant_desk.tooltip.behaviour1", "The villager is _hired_ as a consultant. You need to pay a one-time _hiring bonus_.");
        REGISTRATE.addRawLang("block.createkontor.consultant_desk.tooltip.control1", "When R-Clicked on Employee");
        REGISTRATE.addRawLang("block.createkontor.consultant_desk.tooltip.action1", "Opens the _Consultant's Desk_ interface.");

        REGISTRATE.addRawLang("block.createkontor.logistics_manager_desk.tooltip.summary", "Staffs the company with a _logistics manager_. The manager connects the company to the _logistics network_ of a _Stock Link_ attached to this desk.");
        REGISTRATE.addRawLang("block.createkontor.logistics_manager_desk.tooltip.condition1", "When a Villager sits next to it");
        REGISTRATE.addRawLang("block.createkontor.logistics_manager_desk.tooltip.behaviour1", "The villager is _hired_ as a logistics manager. You need to pay a one-time _hiring bonus_.");
        REGISTRATE.addRawLang("block.createkontor.logistics_manager_desk.tooltip.condition2", "When a Stock Link is attached to it");
        REGISTRATE.addRawLang("block.createkontor.logistics_manager_desk.tooltip.behaviour2", "The company is _connected_ to the network of the link. The goods stored in the network cost a _daily storage fee_.");

        REGISTRATE.addRawLang("wiki.createkontor.category.guides", "Guides & Tips");
        REGISTRATE.addRawLang("wiki.createkontor.category.basics", "Basics");
        REGISTRATE.addRawLang("wiki.createkontor.category.employees", "Employees");
        REGISTRATE.addRawLang("wiki.createkontor.category.blocks", "Blocks");

        REGISTRATE.addRawLang("wiki.createkontor.page.shipping_exit.p1", "The shipping exit allows you to fulfill orders. When powered it will try to fulfill as many orders as possible sorted by urgency. To fulfill an order, all required items need to be present in an adjacent inventory. The items will then be pulled from those inventories.");
        REGISTRATE.addRawLang("wiki.createkontor.page.shipping_exit.scene1", "A basic shipping exit setup. The pulse timer powers the shipping exit regularly.");
        REGISTRATE.addRawLang("wiki.createkontor.page.shipping_exit.p2", "Orders that are in their grace period (= deadline expired) have the highest priority and will be fulfilled first. All other orders come next sorted by their remaining time until the deadline is reached.");
        REGISTRATE.addRawLang("wiki.createkontor.page.shipping_exit.p3", "A comparator can be used to check whether an order has been completed or not. If an order has been completed it will output a power level of 15 for 6 ticks.");

        REGISTRATE.addRawLang("wiki.createkontor.page.getting_started.title", "Getting started");
        REGISTRATE.addRawLang("wiki.createkontor.page.getting_started.p1", "This article provides some basic guidance for new players. It will guide you through founding your company, buying a first license and completing your first order.");
        REGISTRATE.addRawLang("wiki.createkontor.page.getting_started.scene1", "The most basic setup needed to run a company");
        REGISTRATE.addRawLang("wiki.createkontor.page.getting_started.b1", "(0) Found a company");
        REGISTRATE.addRawLang("wiki.createkontor.page.getting_started.p2", "To see this article, you must have already founded a company. But if, for some reason, you have not: Craft a Kontor Desk and right click it. Enter a name and decide whether you want to take the founder's loan. Note that the name cannot be changed afterwards. The founders loan is a cheap loan of %s with %s free days before repayment offered to every founder. After founding, you can right click the Kontor Desk to access the management UI of your company.");
        REGISTRATE.addRawLang("wiki.createkontor.page.getting_started.b2", "(1) Hire a lawyer");
        REGISTRATE.addRawLang("wiki.createkontor.page.getting_started.p3", "To sell items on markets, you need a license for the item you want to sell. A lawyer can buy licenses for you. Craft a Lawyer's Desk and seat a villager next to it using a Create Seat. Right click the villager to open the lawyer UI. Buy a license for your first product(s) in the License Catalog.");
        REGISTRATE.addRawLang("wiki.createkontor.page.getting_started.b3", "(2) Participate in a market");
        REGISTRATE.addRawLang("wiki.createkontor.page.getting_started.p4", "With your new license, you are able to participate in the markets that the bought license covers. Right click the Kontor Desk and participate in one market using the Markets tab. Set a price and you will start receiving requests for that product.");
        REGISTRATE.addRawLang("wiki.createkontor.page.getting_started.b4", "(3) Your first request");
        REGISTRATE.addRawLang("wiki.createkontor.page.getting_started.p5", "Once you participate in a market, you will receive requests for that product. View and accept them in the Requests and Orders tab of the Kontor Desk. An accepted request will turn into an order that you will need to fulfill in time, so start producing the required items to fulfill your first order!");
        REGISTRATE.addRawLang("wiki.createkontor.page.getting_started.b5", "(4) Delivering your first order");
        REGISTRATE.addRawLang("wiki.createkontor.page.getting_started.p6", "Craft a Shipping Exit and place it down next any inventory (e.g. a chest). When powered, the Shipping Exit will try to fulfill any order it can using items from its adjacent inventories. When an order has been completed, the order will disappear from the Order Book and the payout will be booked to your bank account! Congrats! You just delivered your first order!");
        REGISTRATE.addRawLang("wiki.createkontor.page.getting_started.b6", "What's next?");
        REGISTRATE.addRawLang("wiki.createkontor.page.getting_started.p7", "Make money and grow your company. You can upgrade your legal form when necessary. Hire new employees to unlock new features. Expand into new markets and build bigger factories.");

        REGISTRATE.addRawLang("wiki.createkontor.page.daily_settlement.title", "Daily Settlement");
        REGISTRATE.addRawLang("wiki.createkontor.page.daily_settlement.p1", "Every company is settled once at the end of every trading day. A trading day is the same length as one Minecraft day but does not get manipulated by sleeping or changing the time via commands. Sleeping can only bring the trading days and normal minecraft days out of rhythm (e.g. trading days change at noon and not at sunrise when Minecraft days change), but this does not hurt the economy. During the daily settlement, multiple things happen:");
        REGISTRATE.addRawLang("wiki.createkontor.page.daily_settlement.effects", "Business License Fee gets booked\nSalaries get booked\nDaily License Fees get booked\nStorage cost gets booked\nInterest for loans and overdraft gets booked\nLoan repayments get booked\nLegal Form Upgrade Applications may get finished when net worth is sufficient again after resting\nPayment status (normal / illiquid) and Insolvency status are updated\nContracts with vanished employees end");

        REGISTRATE.addRawLang("wiki.createkontor.page.company_activity.title", "Company Activity");
        REGISTRATE.addRawLang("wiki.createkontor.page.company_activity.p1", "A company is active when at least one member (owner or manager) of the company is online. When the company is inactive (= no member online), the entire company is frozen. The company will not receive requests, order deadlines will pause and chunks may get unloaded. Daily settlements occur when the company has been active on the current trading day at least once, even if a member was only online for a minute.");

        REGISTRATE.addRawLang("wiki.createkontor.page.chunk_loading.title", "Chunk Loading");

        REGISTRATE.addRawLang("wiki.createkontor.page.finances.title", "Finances");
        REGISTRATE.addRawLang("wiki.createkontor.page.finances.p1", "Each company has a bank account holding the liquid assets of the company. Salaries, fees, purchases etc. are booked from that bank account. Every company starts with a free %s deposit on its account that does not need repayment.");
        REGISTRATE.addRawLang("wiki.createkontor.page.finances.b1", "Net Worth");
        REGISTRATE.addRawLang("wiki.createkontor.page.finances.p2", "A company's net worth is the balance of a company minus its current liabilities (e.g. loans). You can see your current net worth in the Kontor Desk. Legal Level Upgrades require a minimum net worth.");
        REGISTRATE.addRawLang("wiki.createkontor.page.finances.b2", "Overdraft");
        REGISTRATE.addRawLang("wiki.createkontor.page.finances.p3", "The bank account has an overdraft loan. You can use this loan to overcome temporary liquidity shortages. The overdraft limit defines how much you can overdraft the account and depends on the current legal form of your company. You can see your current overdraft limit in the Kontor Desk. When using the overdraft credit, you will pay daily interest for the borrowed money. If you exceed your overdraft limit your company will get illiquid and is not able to spend money anymore.");
        REGISTRATE.addRawLang("wiki.createkontor.page.finances.b3", "Illiquidity");
        REGISTRATE.addRawLang("wiki.createkontor.page.finances.p4", "A company's payment status is illiquid, when the overdraft limit has been exceeded. Your liquid assets have been fully exhausted. In this state, you cannot spend anymore money. You can see your current payment status in the Kontor Desk.");
        REGISTRATE.addRawLang("wiki.createkontor.page.finances.b4", "Insolvency");
        REGISTRATE.addRawLang("wiki.createkontor.page.finances.p5", "A company is insolvent, when it has been illiquid for more than %s days in a row.");
        REGISTRATE.addRawLang("wiki.createkontor.page.finances.b5", "Bankruptcy");
        REGISTRATE.addRawLang("wiki.createkontor.page.finances.p6", "A company is eligible for bankruptcy when it is insolvent. The owner can then declare bankruptcy in the Kontor Desk. Declaring bankruptcy will permanently delete the company so you can start over again with a new company.");

        REGISTRATE.addRawLang("wiki.createkontor.page.legal_forms.title", "Legal Forms");
        REGISTRATE.addRawLang("wiki.createkontor.page.legal_forms.p1", "Legal forms are the legal foundation of a company. They define what a company can do and what it cannot do. You can see your current legal form in the Kontor Desk. A lawyer can tell you the exact limits of your current legal form.");
        REGISTRATE.addRawLang("wiki.createkontor.page.legal_forms.b1", "Upgrading legal forms");
        REGISTRATE.addRawLang("wiki.createkontor.page.legal_forms.p2", "A lawyer can upgrade the legal form of your company. To upgrade a legal form, several requirements must be met:");
        REGISTRATE.addRawLang("wiki.createkontor.page.legal_forms.requirements", "Minimum net worth plus application fee\nMinimum amount of fulfilled orders\nMinimum reputation\nNo current payment difficulties");
        REGISTRATE.addRawLang("wiki.createkontor.page.legal_forms.p3", "The exact requirements depend on the legal form that you want to upgrade to. A lawyer can tell you the exact requirements. Additionally, an application fee must be paid.");
        REGISTRATE.addRawLang("wiki.createkontor.page.legal_forms.p4", "The upgrade application takes some time to process. During this processing time your company needs a lawyer at all times or the application will be cancelled and the fee refunded. If your company does not have sufficient net worth at the end of the application it will start to rest. It will rest for a few days depending on the applied legal form. After that the application will be rejected and the fee is lost.");

        REGISTRATE.addRawLang("wiki.createkontor.page.markets.title", "Markets");
        REGISTRATE.addRawLang("wiki.createkontor.page.markets.p1", "There are many simulated markets for different items. Each market has a price, demand, simulated competitors and overflow. You can analyse these factors using a market analyst.");
        REGISTRATE.addRawLang("wiki.createkontor.page.markets.b1", "Price");
        REGISTRATE.addRawLang("wiki.createkontor.page.markets.p2", "The price of a market is made up of two components: Price Level and Price Deviation. Price Level is the structural level of a product depending on competitor utilisation and reference cost (= the minimum cost of a product derived from the recipe tree). It is updated once per trading day. The Price Deviation is added onto the price level and makes prices respond directly when a company sells or buys large amounts of items. It is updated several times during a trading day. A price deviation is only temporary and prices will sooner or later return to their price level.");
        REGISTRATE.addRawLang("wiki.createkontor.page.markets.b2", "Demand");
        REGISTRATE.addRawLang("wiki.createkontor.page.markets.p3", "Each market has a demand which is determined by a base demand times and the current economic phase. In the long run demand will always rise and companies will have to keep up with that.");
        REGISTRATE.addRawLang("wiki.createkontor.page.markets.b3", "Competitors");
        REGISTRATE.addRawLang("wiki.createkontor.page.markets.p4", "Every market also simulates competitors alongside player-run companies in the market. Simulated competitors are not full companies but rather the result of several calculations. Competitors will get founded or go bankrupt. The amount of competitors therefor always fluctuates. The production of competitors grows in the long run and they are able to keep up with rising demand. Competitors always sell at market price with a reputation of 3.0.");
        REGISTRATE.addRawLang("wiki.createkontor.page.markets.b4", "Overflow");
        REGISTRATE.addRawLang("wiki.createkontor.page.markets.p5", "When a market does not have enough production capacity, demand can occasionally go unserved. This creates overflow (more demand than competitors and player-run companies can keep up with) and may be an opportunity for new companies to enter the market.");

        REGISTRATE.addRawLang("wiki.createkontor.page.licenses.title", "Licenses");
        REGISTRATE.addRawLang("wiki.createkontor.page.licenses.p1", "To be able to sell items on a market, you need a license for that market. You can buy licenses in the license catalog using a lawyer. Licenses might require you to have a certain legal form in order to buy them. Additionally, when acquiring a license you will need to pay a one-time activation fee.");
        REGISTRATE.addRawLang("wiki.createkontor.page.licenses.b1", "Daily Fee & Revenue Share");
        REGISTRATE.addRawLang("wiki.createkontor.page.licenses.p2", "There are two main payment models for licenses: Daily Fee and Revenue Share. The daily fee is, well, a fee that will be booked daily as long as you hold the license. The revenue share is a share of the revenue of each order that you complete using this license.");
        REGISTRATE.addRawLang("wiki.createkontor.page.licenses.b2", "Bundles");
        REGISTRATE.addRawLang("wiki.createkontor.page.licenses.p3", "Many licenses come as bundles, meaning you get licensed for multiple markets. This can lead to overlaps: You can e.g. hold two licenses that both license you to sell iron ingots. In that case, you will need to pay the daily fee for both licenses. But when completing an order, e.g. for iron ingots, you will pay the lowest revenue share of all iron ingot licenses you hold.");
        REGISTRATE.addRawLang("wiki.createkontor.page.licenses.b3", "Cancelling");
        REGISTRATE.addRawLang("wiki.createkontor.page.licenses.p4", "You can cancel a license at any time. After cancelling, you will immediately withdraw from any markets that are not covered by a license anymore. With it, you will lose your reputation on markets that you are not licensed for anymore.");

        REGISTRATE.addRawLang("wiki.createkontor.page.market_participation.title", "Market Participation");
        REGISTRATE.addRawLang("wiki.createkontor.page.market_participation.p1", "Holding a license for a market does not mean that you automatically participate in the market. Instead you can control your participation independently, as long as you have at least one license for the market. In the Kontor Desk you can decide whether to participate in a market. When you participate, you can set a price and will start to receive requests for the product. Your reputation on the market is not influenced when you decide to stop participating. The daily fee of a license is booked regardless of whether you participate in the market or not.");

        REGISTRATE.addRawLang("wiki.createkontor.page.requests_orders.title", "Requests & Orders");
        REGISTRATE.addRawLang("wiki.createkontor.page.requests_orders.p1", "When you participate in a market, you will start to receive requests for that product. A request only stays on the board for a short amount of time in which you can accept it before disappearing.");
        REGISTRATE.addRawLang("wiki.createkontor.page.requests_orders.p2", "An accepted request turns into an order. You will now need to fulfill the order in time or your reputation gets hurt. Use the shipping exit to fulfill orders.");
        REGISTRATE.addRawLang("wiki.createkontor.page.requests_orders.p3", "When the deadline of an order expires, a grace period starts. In this short period, you can still fulfill the order, but you will only receive a partial payout.");
        REGISTRATE.addRawLang("wiki.createkontor.page.requests_orders.p4", "The higher your legal level is, the shorter deadlines are and the larger the orders are.");

        REGISTRATE.addRawLang("wiki.createkontor.page.reputation.title", "Reputation");
        REGISTRATE.addRawLang("wiki.createkontor.page.reputation.p1", "Each company has a reputation for each market it participates in. Reputation is one of the two factors directly determining the market share. Successfully completing an order increases your reputation for that product. Failing to complete an order hurts your reputation. In a fresh market, you will start with a neutral reputation of 3.0 stars. You can see your reputation in a market using a market analyst.");
        REGISTRATE.addRawLang("wiki.createkontor.page.reputation.p2", "If you do not complete any orders in a market for one trading day, your reputation will slowly drift back to 3.0 stars.");
        REGISTRATE.addRawLang("wiki.createkontor.page.reputation.p3", "Legal level upgrades require a minimum overall reputation. This overall reputation is the weighted average of all individual market reputations.");

        REGISTRATE.addRawLang("wiki.createkontor.page.market_share.title", "Market Share");
        REGISTRATE.addRawLang("wiki.createkontor.page.market_share.p1", "The market share determines how much of the daily demand you will receive in the form of requests. You can see your market share using a market analyst.");
        REGISTRATE.addRawLang("wiki.createkontor.page.market_share.p2", "The market share is made up of your price and your reputation. Selling below market price with a high reputation will get you more market share than selling above market price with a low reputation.");

        REGISTRATE.addRawLang("wiki.createkontor.page.managers.title", "Managers");
        REGISTRATE.addRawLang("wiki.createkontor.page.managers.p1", "You can run a company together with other players. The owner can invite other players to their company as managers using the Kontor Desk. A player can always be manager or owner of only one company at a time. Managers can either leave a company again by themselves or they can get fired by the owner.");
        REGISTRATE.addRawLang("wiki.createkontor.page.managers.p2", "Managers can do everything the owner can, except for inviting / firing managers and declaring bankruptcy.");

        REGISTRATE.addRawLang("wiki.createkontor.page.economic_cycle.title", "Economic Cycle");

        REGISTRATE.addRawLang("wiki.createkontor.page.policy_rate.title", "Policy Rate");

        REGISTRATE.addRawLang("wiki.createkontor.page.employees.title", "Employees");

        REGISTRATE.addRawLang("wiki.createkontor.page.lawyer.title", "Lawyer");

        REGISTRATE.addRawLang("wiki.createkontor.page.market_analyst.title", "Market Analyst");

        REGISTRATE.addRawLang("wiki.createkontor.page.buyer.title", "Buyer");

        REGISTRATE.addRawLang("wiki.createkontor.page.logistics_manager.p1", "The logistics manager allows you to link a Create Logistics Network to your company. A company may only have one linked logistics network at a time. The owner of the logistics network must be a member of the company. Linking a logistics network requires a minimum legal level.");
        REGISTRATE.addRawLang("wiki.createkontor.page.logistics_manager.b1", "Linking a logistics network");
        REGISTRATE.addRawLang("wiki.createkontor.page.logistics_manager.scene1", "A Logistics Manager with a connected logistics network.");
        REGISTRATE.addRawLang("wiki.createkontor.page.logistics_manager.p2", "You can connect a logistics network to your company, by attaching a stock link to the staffed Logistics Manager's Desk. A chat message will inform every online member of the company that a logistics network has been linked. Equally, a chat message will notify every online member when the logistics network gets disconnected.");
        REGISTRATE.addRawLang("wiki.createkontor.page.logistics_manager.b2", "Storage Pricing");
        REGISTRATE.addRawLang("wiki.createkontor.page.logistics_manager.p3", "Managing your storage costs money (in addition to the salary of the Logistics Manager). One item costs %s per day. The fee depends on how many items were stored how long in the logistics network and is billed at the end of each trading day. When the network is disconnected, the fee for the current trading day so far is booked instantly. The Logistics Manager can show you an estimate of the storage cost per day.");
        REGISTRATE.addRawLang("wiki.createkontor.page.logistics_manager.b3", "Automatic Order Delivery");
        REGISTRATE.addRawLang("wiki.createkontor.page.logistics_manager.p4", "The Logistics Manager can automatically send items needed to fulfill orders from the connected Logistics Network to a set target address. When correctly combined with a Shipping Exit you can automate order delivery using the Logistics Manager.");
        REGISTRATE.addRawLang("wiki.createkontor.page.logistics_manager.p5", "While Automatic Order Delivery is on, the Logistics Manager regularly checks the open orders of your company, in the same priority order as the Shipping Exit, and orders the items that are in stock. Unlike the Shipping Exit, he does not wait for the full quantity of an order, he orders what is available. The packages are sent to the target address. From there, your own logistics have to bring them to a Shipping Exit, which then delivers the order. To avoid ordering the same items twice, the Logistics Manager keeps track of how many items he has already ordered for each order. If any items get lost on the way, you can reset the tracked progress for one order or for all of them.");
        REGISTRATE.addRawLang("wiki.createkontor.page.logistics_manager.p6", "The minimum stock applies to each item separately. The Logistics Manager only orders what exceeds the minimum stock and leaves the rest in the Logistics Network.");
    }

    static void touch() {
    }

    private KontorLanguage() {
    }
}
