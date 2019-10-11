package us.oyanglul.zhuyu

package object jobs
    extends OnPaymentInited
    with OnPaymentDebited
    with OnDebitEntryFileUploaded
    with OnDebitEntryProcessed
    with OnBillingServiceNotified
