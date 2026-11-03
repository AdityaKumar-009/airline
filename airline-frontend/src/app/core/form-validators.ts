// Shared input rules keep browser forms consistent across pages.
export const NAME_PATTERN = /^ *\p{L}+(?: +\p{L}+)* *$/u;
export const EMAIL_DOMAIN_PATTERN = /^[^\s@]+@[^\s@]+\.[A-Za-z]{2,}$/;
export const ALPHANUMERIC_PATTERN = /^[A-Za-z0-9]+$/;
export const INDIAN_PHONE_PATTERN = /^$|^[6-9]\d{9}$/;
