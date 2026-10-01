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

        REGISTRATE.addRawLang("ui.createkontor.chart.no_data.detailed", "There is no data for the chart \"%s\" yet. You can try again later.");
        REGISTRATE.addRawLang("ui.createkontor.chart.no_data.short", "There is no data for this chart yet. You can try again later.");

        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.business_fee.info", "§7A business fee of %s is charged every day and debited from the company's bank account.");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.deposit.info", "§7Every company receives a free deposit of %s that does not need to be payed back.");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.founders_loan.info", "§7Additionally, you can take a founders loan of %s with %s free days before you need to repay it.");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.founding", "Found company");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.founding_title", "Found a new company");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.company_name", "Company Name");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.overview", "Overview");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.requests_orders", "Requests & Orders");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.account", "Bank Account");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.balance", "Balance");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.cost_structure", "Cost Structure");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.bookings", "Bookings");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.tab.open_loans", "Open Loans");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.open_orders", "Open Orders: %s");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.total_bookings_visible", "Visible Bookings: %s");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.current_overdraft_limit", "Overdraft Limit: %s");
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

        REGISTRATE.addRawLang("enum.createkontor.liquidity.normal", "§2Normal Liquidity");
        REGISTRATE.addRawLang("enum.createkontor.liquidity.illiquidity", "§4Illiquidity");

        REGISTRATE.addRawLang("enum.createkontor.booking_kind.business_license", "Business License");
        REGISTRATE.addRawLang("enum.createkontor.booking_kind.flat_license", "Flat Product License");
        REGISTRATE.addRawLang("enum.createkontor.booking_kind.turnover_license", "Turnover Product License");
        REGISTRATE.addRawLang("enum.createkontor.booking_kind.application_fee", "Application Fee");
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

        REGISTRATE.addRawLang("legal_forms.createkontor.sole_proprietorship", "§7Sole Proprietorship");
        REGISTRATE.addRawLang("legal_forms.createkontor.partnership", "§bPartnership");
        REGISTRATE.addRawLang("legal_forms.createkontor.limited_company", "§5Limited Company");
        REGISTRATE.addRawLang("legal_forms.createkontor.public_company", "§6Public Company");

        REGISTRATE.addRawLang("license.createkontor.sheet_metal_bundle", "Sheet Metal Bundle License");
        REGISTRATE.addRawLang("license.createkontor.generated", "%s License");

        REGISTRATE.addRawLang("employee_role.createkontor.lawyer", "Lawyer");

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
    }

    static void touch() {
    }

    private KontorLanguage() {
    }
}
